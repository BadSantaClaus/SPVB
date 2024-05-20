package utils;

import constants.FileName;
import lombok.SneakyThrows;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static constants.DateFormat.XML_TIME_FORMAT;
import static constants.FilePath.TEMP_FILES;
import static constants.FilePath.XML_FILE_TEMPLATE;
import static java.time.format.DateTimeFormatter.ofPattern;

@SuppressWarnings({"UnusedReturnValue,unchecked"})
public class XmlUtils {

    private final static SAXReader reader = new SAXReader();

    public static Document parseXml(FileName templateName) {
        return parseXml(String.format(XML_FILE_TEMPLATE.getValue(), templateName.getValue()));
    }

    @SneakyThrows
    public static Document parseXml(String path) {
        return reader.read(path);
    }

    @SneakyThrows
    public static Document parseXml(File file) {
        return reader.read(file);
    }

    @SneakyThrows
    public static Document parseStringToDocument(String str) {
        return reader.read(new StringReader(str));
    }

    @SneakyThrows
    public static Document toXml(Document document, String filePath) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("windows-1251");
        XMLWriter out = null;
        try {
            out = new XMLWriter(new FileOutputStream(filePath), format);
            out.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
        return parseXml(filePath);

    }

    public static LocalDateTime getTimeFromPfx(FileName pfx, String node, String stockCode) {
        return getTimeFromPfx(parseXml(pfx), node, stockCode);
    }

    public static LocalDateTime getTimeFromPfx(String path, String node, String stockCode) {
        return getTimeFromPfx(parseXml(path), node, stockCode);
    }

    public static LocalDateTime getTimeFromPfx(Document doc, String node, String stockCode) {
        List<Node> list = doc.selectNodes("//AUCTION");
        String start = list.stream()
                .filter(n -> n.selectSingleNode("@SECURITYID").getText().equals(stockCode))
                .map(n -> n.selectSingleNode(String.format("@%s", node)).getText())
                .findFirst()
                .orElseThrow();
        LocalTime time = LocalTime.parse(start, ofPattern(XML_TIME_FORMAT.getValue()));
        return time.atDate(LocalDate.now());
    }

    public static LocalDateTime getTimeFromPfx02(String fileName, FileName fileTemplate, String node) {
        Document doc = parseXml(TEMP_FILES.getValue() + fileName);
        String timeStr;
        if (fileTemplate.equals(FileName.PFX02))
            timeStr = doc.selectSingleNode(String.format("//AUCTION/@%s", node)).getText();
        else
            timeStr = doc.selectSingleNode(String.format("//FIX/@%s", node)).getText();
        LocalTime time = LocalTime.parse(timeStr, ofPattern(XML_TIME_FORMAT.getValue()));
        return time.atDate(LocalDate.now());
    }

    public static LocalDateTime getTimeFromRepoLo(String filName, String node) {
        Document doc = parseXml(TEMP_FILES.getValue() + filName);
        String timeStr = doc.selectSingleNode("//selection_info_rec/@" + node).getText();
        LocalTime time = LocalTime.parse(timeStr, ofPattern(XML_TIME_FORMAT.getValue()));
        return time.atDate(LocalDate.now());
    }
}
