package utils;

import com.codeborne.selenide.Selenide;
import com.jcraft.jsch.*;
import constants.Credentials;
import constants.FileName;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@SuppressWarnings({"UnusedReturnValue"})
public class SftpUtils {
    private static Session session;
    private static Channel channel;

    @SneakyThrows
    private static void setupSession(String user) {
        JSch jsch = new JSch();
        session = jsch.getSession(
                user,
                Credentials.getInstance().sshHost(),
                Credentials.getInstance().sshPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(Credentials.getInstance().sshLoginPassword());
        session.connect();
    }

    @SneakyThrows
    private static void setupSftp() {
        setupSession(Credentials.getInstance().sshSftpLogin());
        channel = session.openChannel("sftp");
        channel.connect();
        Runtime.getRuntime().addShutdownHook(new Thread(SftpUtils::disconnect));
    }

    @SneakyThrows
    private void setupCommandExecutor() {
        setupSession(Credentials.getInstance().sshCryptoLogin());
        channel = session.openChannel("shell");
        channel.connect();
    }

    private static void disconnect() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    @SneakyThrows
    public static void uploadFile(Path localFilePath, String remoteDir) {
        String fileName = localFilePath.getFileName().toString();
        setupSftp();
        ((ChannelSftp) channel).put(localFilePath.toString(), remoteDir + fileName);
        SpvbUtils.step(String.format("Поместить файл [%s] на сфтп в папку %s", fileName, remoteDir));
    }

    @SneakyThrows
    public static void downloadFile(String fileName, String remotePath, String localPath) {
        setupSftp();
        ((ChannelSftp) channel).get(remotePath + fileName, localPath + fileName);
    }

    @SneakyThrows
    public static void deleteFile(String sftpFilePath) {
        setupSftp();
        ((ChannelSftp) channel).rm(sftpFilePath);
        log.info("Удалить файл " + sftpFilePath);
    }

    @SneakyThrows
    public void executeCommand(String command) {
        setupCommandExecutor();
        InputStream inputStream = channel.getInputStream();
        OutputStream out = channel.getOutputStream();
        PrintStream commander = new PrintStream(out, true);
        waitCmdInputStreamContains(inputStream, session.getUserName());
        commander.println(command);
        waitCmdInputStreamContains(inputStream, session.getUserName());
    }

    @SneakyThrows
    public void executeCommandWithPassword(String command, String password) {
        setupCommandExecutor();
        InputStream inputStream = channel.getInputStream();
        OutputStream out = channel.getOutputStream();
        PrintStream commander = new PrintStream(out, true);
        waitCmdInputStreamContains(inputStream, session.getUserName());
        commander.println(command);
        waitCmdInputStreamContains(inputStream, "password");
        commander.println(password);
        waitCmdInputStreamContains(inputStream, session.getUserName());
    }

    private String readAll(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (inputStream.available() == 0)
            sb.append((char) inputStream.read());
        while (inputStream.available() > 1) {
            sb.append(new String(inputStream.readNBytes(inputStream.available() - 1)));
        }
        return sb.toString();
    }

    private void waitCmdInputStreamContains(InputStream inputStream, String expectedStr) throws IOException {
        String str = readAll(inputStream);
        while (!str.contains(expectedStr)) {
            Selenide.sleep(1000);
            str = readAll(inputStream);
        }
    }

    @SneakyThrows
    public List<String> getAllFileNamesInDirectory(String directory) {
        try{
        List<String> result = new LinkedList<>();
        setupSftp();
        List<?> ls = ((ChannelSftp) channel).ls(directory);
        for (Object e : ls) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) e;
            result.add(entry.getFilename());
        }
        return result;}
        catch (Throwable throwable){
            List<String> list = new ArrayList<>();
            return list;
        }
    }

    public List<String> getFileNamesByTemplateInDir(FileName nameTemplate, String extension, String directory) {
        List<String> fileNames = getAllFileNamesInDirectory(directory);
        return fileNames.stream().filter(a -> a.matches(nameTemplate.getValue() + "_\\d+_\\d+" + extension)).toList();
    }

    @SneakyThrows
    public SftpATTRS getFileAttrs(String path) {
        return ((ChannelSftp) channel).lstat(path);
    }
}
