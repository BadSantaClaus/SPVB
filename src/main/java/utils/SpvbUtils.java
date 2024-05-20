package utils;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.jcraft.jsch.SftpATTRS;
import constants.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import static constants.DateFormat.FILE_DATE_FORMAT;
import static constants.DateFormat.SFTP_DATE_FORMAT1;
import static constants.Extension.XML;
import static constants.FileName.MKR_SECURITY_EXPORT;
import static constants.FilePath.TEMP_FILES;

@Slf4j
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpvbUtils {
    public static void step(String message) {
        log.info(message);
        Allure.step(message);
    }

    public static String generateFileNameIncreaseIndex(FileName nameTemplate, Extension extension, FilePath folder) {
        List<String> filesInSftp = new SftpUtils().getAllFileNamesInDirectory(folder.getValue());
        filesInSftp = filesInSftp.stream()
                .filter(a -> a.matches(nameTemplate.getValue() + "_\\d+_\\d+" + extension.getValue()))
                .sorted().toList();

        int maxIdx = 0;
        if (!filesInSftp.isEmpty())
            maxIdx = filesInSftp.stream().map(a -> Integer.parseInt(a.split("_")[1]))
                    .max(Integer::compare).get();

        String fileTemplate = nameTemplate.getValue() + "_%s_%s" + extension.getValue();
        String result = String.format(fileTemplate, maxIdx + 1, LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATE_FORMAT.getValue())));
        log.info("Сгенерировано имя - {}", result);
        return result;
    }

    public static String generateFileNameIncreaseIndex(FileName nameTemplate, Extension extensionOnSftp, Extension newExtension,  FilePath folder) {
        List<String> filesInSftp = new SftpUtils().getAllFileNamesInDirectory(folder.getValue());
        filesInSftp = filesInSftp.stream()
                .filter(a -> a.matches(nameTemplate.getValue() + "_\\d+_\\d+" + extensionOnSftp.getValue()))
                .sorted().toList();

        int maxIdx = 0;
        if (!filesInSftp.isEmpty())
            maxIdx = filesInSftp.stream().map(a -> Integer.parseInt(a.split("_")[1]))
                    .max(Integer::compare).get();

        String fileTemplate = nameTemplate.getValue() + "_%s_%s" + newExtension.getValue();
        String result = String.format(fileTemplate, maxIdx + 1, LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATE_FORMAT.getValue())));
        log.info("Сгенерировано имя - {}", result);
        return result;
    }

    public static String generateMkrFileName(FilePath folder) {
        List<String> filesInSftp = new SftpUtils().getAllFileNamesInDirectory(folder.getValue());
        filesInSftp = filesInSftp.stream()
                .filter(a -> a.matches(MKR_SECURITY_EXPORT.getValue() + "_\\d+_\\d+" + XML.getValue()))
                .map(f -> f.replaceAll(XML.getValue(), ""))
                .sorted().toList();

        int maxIdx = 0;
        if (!filesInSftp.isEmpty())
            maxIdx = filesInSftp.stream().map(a -> Integer.parseInt(a.split("_")[4]))
                    .max(Integer::compare).get();

        String fileTemplate = MKR_SECURITY_EXPORT.getValue() + "_%s_%s" + XML.getValue();
        String idx = String.format("%03d", maxIdx + 1);
        String result = String.format(fileTemplate, LocalDateTime.now().format(DateTimeFormatter.ofPattern(SFTP_DATE_FORMAT1.getValue())), idx);
        log.info("Сгенерировано имя - {}", result);
        return result;
    }

    public static String generateFileNameSaveIndex(FileName nameTemplate, Extension extension, FilePath folder) {
        List<String> filesInSftp = new SftpUtils().getAllFileNamesInDirectory(folder.getValue());
        filesInSftp = filesInSftp.stream()
                .filter(a -> a.matches(nameTemplate.getValue() + "_\\d+_\\d+" + extension.getValue()))
                .sorted()
                .toList();
        int maxIdx;
        if (!filesInSftp.isEmpty()) {
            maxIdx = filesInSftp.stream().map(a -> Integer.parseInt(a.split("_")[1]))
                    .max(Integer::compare).get();
            String fileTemplate = nameTemplate.getValue() + "_%s_%s" + extension.getValue();
            String result = String.format(fileTemplate, maxIdx, LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATE_FORMAT.getValue())));
            log.info("Сгенерировано имя - {}", result);
            return result;
        } else {
            String fileTemplate = nameTemplate.getValue() + "_%s_%s" + extension.getValue();
            String result = String.format(fileTemplate, 1, LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATE_FORMAT.getValue())));
            log.info("Сгенерировано имя - {}", result);
            return result;
        }
    }

    public static String getFileNameCreatedBetween(FileName template, String extension, FilePath filePath,
                                                   LocalDateTime left, LocalDateTime right
    ) {
        SftpUtils sftpUtils = new SftpUtils();
        List<String> filesInSftp = sftpUtils.getFileNamesByTemplateInDir(template, extension, filePath.getValue());
        filesInSftp = filesInSftp.stream().filter(f -> {
            SftpATTRS attrs = sftpUtils.getFileAttrs(filePath.getValue() + f);
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
            LocalDateTime createFileDateTime = LocalDateTime
                    .ofEpochSecond(attrs.getMTime(), 999999999, zoneOffset);
            return !left.isAfter(createFileDateTime) && !right.isBefore(createFileDateTime);
        }).toList();
        String result = filesInSftp.get(0);
        log.info("Сгенерировано имя - {}", result);
        return result;
    }

    public static void createTempFilesDir() {
        String dirPath = TEMP_FILES.getValue();
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String getFileExtension(String path) {
        return path.substring(path.length() - 3);
    }

    @SneakyThrows
    public static void cleanQuikFiles() {
        Runtime.getRuntime().exec(CmdCommand.CLOSE_QUIK.getValue());
        WaitingUtils.sleep(1);
        new File("acnt.dat").delete();
        new File("alert.ik").delete();
        new File("alerts.dat").delete();
        new File("alltrade.dat").delete();
        new File("banners.dat").delete();
        new File("classes.dat").delete();
        new File("curr_data.log").delete();
        new File("firms.dat").delete();
        new File("info.log").delete();
        new File("info.sav.wnd").delete();
        new File("info.wnd").delete();
        new File("limits.dat").delete();
        new File("locales.dat").delete();
        new File("orders.dat").delete();
        new File("par.dat").delete();
        new File("portfolio.dat").delete();
        new File("portfolio.log").delete();
        new File("sec.dat").delete();
        new File("tmsg.dat").delete();
        new File("tradermsg.dat").delete();
        new File("trades.dat").delete();
        new File("trans.dat").delete();
        new File("transresult.dat").delete();
        new File("smask.bin").delete();
        new File("qchat_cached.dat").delete();
        new File("target/temp/table.txt").delete();
        new File("target/temp/request_table.txt").delete();
        FileUtils.deleteDirectory(new File("WNDSAV"));
    }

    @SneakyThrows
    public static void cleanTempFiles() {
        if (Files.exists(Path.of("target/temp")))
            FileUtils.deleteDirectory(new File("target/temp"));
    }

    public static String getAucId(String path) {
        return XmlUtils.parseXml(path)
                .selectSingleNode("//AUCTION/@AUCTION_ID")
                .getText();
    }

    public static String getAucId(String path, String xpath) {
        return XmlUtils.parseXml(path)
                .selectSingleNode(xpath)
                .getText();
    }

    @SneakyThrows
    public static Document getNodeFromP7sFile(String filePath, String tag) {
        String str = new String(Files.readAllBytes(Path.of(filePath)), "windows-1251");
        String str1 = str.substring(
                str.indexOf(String.format("<%s>", tag)),
                str.lastIndexOf(String.format("</%s>", tag)) + tag.length() + 3);
        return XmlUtils.parseStringToDocument(str1);
    }

    @Attachment(value = "web screenshot", type = "image/png")
    public static byte[] takeScreenshot() {
        String screen64 = Selenide.screenshot(OutputType.BASE64);
        if (screen64 != null) {
            return Base64.getDecoder().decode(screen64);
        } else return null;
    }

    @SneakyThrows
    public static void takeNameScreenshot(String name) {
        byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(screenshot)) {
            Allure.addAttachment(name, bis);
        }
    }

    public static String currTimePlusMin(int plusMinutes, DateFormat format) {
        return LocalDateTime.now().plusMinutes(plusMinutes).format(DateTimeFormatter.ofPattern(format.getValue()));
    }

    @SneakyThrows
    public static String readFromFile(String path) {
        try (FileInputStream file = new FileInputStream(path)) {
            return IOUtils.toString(file, "Windows-1251");
        }
    }

}
