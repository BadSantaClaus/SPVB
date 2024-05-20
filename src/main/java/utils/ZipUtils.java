package utils;

import constants.Extension;
import lombok.SneakyThrows;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.List;

@SuppressWarnings("unchecked")
public class ZipUtils {

    @SneakyThrows
    public static File unzipFileWithInternalZip(File zipFile) {
        ZipFile externalZip = new ZipFile(zipFile);
        List<FileHeader> externalHeaders = externalZip.getFileHeaders();
        String externalPath = externalHeaders.stream()
                .map(FileHeader::getFileName)
                .filter(s -> s.endsWith(Extension.ZIP.getValue()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Вложенный архив не найден"));
        externalZip.extractAll("target/temp/pre-extracted");
        String preExtractedPath = "target/temp/pre-extracted/" + externalPath;
        ZipFile internalZip = new ZipFile(preExtractedPath);
        List<FileHeader> internalHeaders = internalZip.getFileHeaders();
        String filePath = internalHeaders.stream()
                .map(FileHeader::getFileName)
                .filter(s -> s.endsWith(Extension.TRI.getValue()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Вложенный архив не найден"));
        internalZip.extractAll("target/temp/full-extracted");
        String fullPath = "target/temp/full-extracted/" + filePath;
        return new File(fullPath);
    }
}
