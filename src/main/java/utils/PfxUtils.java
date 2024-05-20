package utils;

import constants.AutomatingBlock;
import constants.FileName;
import constants.XmlAttribute;
import db.auction.AuctionDbHelper;
import db.auction.MkrDbHelper;
import db.initiators.overCountRepo.OverCountRepoDbHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.dbo.AuctionDbo;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static constants.DateFormat.XML_DATE_FORMAT;
import static constants.DateFormat.XML_TIME_FORMAT;
import static constants.Extension.XML;
import static constants.Extension.XML_P7S;
import static constants.FileName.*;
import static constants.FilePath.*;
import static constants.XmlAttribute.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.format.DateTimeFormatter.ofPattern;

@Slf4j
@SuppressWarnings({"unchecked", "UnusedReturnValue", "ResultOfMethodCallIgnored"})
public class PfxUtils {

    private static final DateTimeFormatter dateFormatter = ofPattern(XML_DATE_FORMAT.getValue());

    @SneakyThrows
    public static File createZFile(FileName pfxFormat) {
        String filePath = FILE_TEMPLATES_DIR.getValue() + "zPFX" + XML.getValue();
        String newFilePath = TEMP_FILES.getValue() + SpvbUtils.generateFileNameIncreaseIndex(pfxFormat, XML_P7S, XML, BR_IN);
        Files.copy(Path.of(filePath), Path.of(newFilePath), REPLACE_EXISTING);
        return new File(newFilePath);
    }

    public static Document parseXmlWithDateAndAucId(FileName pfx, String pfxName) {
        Document doc = XmlUtils.parseXml(pfx);
        List<Node> list = doc.selectNodes("//AUCTION");
        for (Node el : list) {
            el.selectSingleNode("@AUCTION_ID").setText(generateAuctionId(AutomatingBlock.BR_OFZ));
            el.selectSingleNode("@AUC_DATE").setText(LocalDateTime.now().format(dateFormatter));
        }
        log.info("Создать {} файл", pfxName);
        return XmlUtils.toXml(doc, TEMP_FILES.getValue() + pfxName);
    }

    @SneakyThrows
    public static Document preparePfx39(String pfx38Name) {
        String pfx38Path = TEMP_FILES.getValue() + pfx38Name;
        SftpUtils.downloadFile(pfx38Name, BR_OUT_NO_SIGN.getValue(), TEMP_FILES.getValue());
        Document doc = XmlUtils.parseXml(pfx38Path);
        doc = DocumentHelper.parseText(doc.asXML().replace("PFX38", "PFX39"));
        doc.selectSingleNode("//DOC_REQUISITIONS/@SENDER_ID").setText("MZ0000200000");
        doc.selectSingleNode("//DOC_REQUISITIONS/@SENDER_NAME").setText("");
        doc.selectSingleNode("//DOC_REQUISITIONS/@RECEIVER_ID").setText("PM0020500000");
        return doc;
    }

    @SneakyThrows
    public static void createPfx39_t113(String pfx38Name, String pfx39Name) {
        Document doc = preparePfx39(pfx38Name);
        List<Node> requests = doc.selectNodes("//REQUEST");
        Node request1 = requests.get(0);
        Node request2 = requests.get(1);
        Node request3 = requests.get(2);
        acceptRequest(request1);
        acceptRequest(request2);
        request2.selectSingleNode("@RQ_QUANTITY").setText("6000");
        rejectRequest(request3);
        XmlUtils.toXml(doc, TEMP_FILES.getValue() + pfx39Name);
    }

    @SneakyThrows
    public static void createPfx39_t97(String pfx38Name, String pfx39Name) {
        Document doc = preparePfx39(pfx38Name);
        List<Node> requests = doc.selectNodes("//REQUEST");
        Node request1 = requests.get(0);
        Node request2 = requests.get(1);
        rejectRequest(request1);
        rejectRequest(request2);
        XmlUtils.toXml(doc, TEMP_FILES.getValue() + pfx39Name);
    }

    @SneakyThrows
    public static void createPfx39_t87(String pfx38Name, String pfx39Name) {
        Document doc = preparePfx39(pfx38Name);
        List<Node> requests = doc.selectNodes("//REQUEST");
        Node request1 = requests.get(0);
        Node request2 = requests.get(1);
        acceptRequest(request1);
        acceptRequest(request2);
        XmlUtils.toXml(doc, TEMP_FILES.getValue() + pfx39Name);
    }

    public static void rejectRequest(Node node) {
        node.selectSingleNode("@RQ_STATUS").setText("R");
        node.selectSingleNode("@RQ_YIELD").setText("");
        node.selectSingleNode("@RQ_ACC").setText("");
        node.selectSingleNode("@RQ_VALUE").setText("");
        node.selectSingleNode("@RQ_QUANTITY").setText("");
        node.selectSingleNode("@RQ_PRICE").setText("");
    }

    public static void acceptRequest(Node node) {
        node.selectSingleNode("@RQ_STATUS").setText("A");
    }

    @SneakyThrows
    public static Document createPfx43(String pfx43RemoteName, String pfx43IncreaseIndexName) {
        String pfx43RemoteNamePath = TEMP_FILES.getValue() + pfx43RemoteName;
        String pfx43IncreaseIndexNamePath = TEMP_FILES.getValue() + pfx43IncreaseIndexName;
        SftpUtils.downloadFile(pfx43RemoteName, BR_OUT_NO_SIGN.getValue(), TEMP_FILES.getValue());
        File pfx43Remote = new File(pfx43RemoteNamePath);
        pfx43Remote.renameTo(new File(pfx43IncreaseIndexNamePath));
        return XmlUtils.parseXml(pfx43IncreaseIndexNamePath);
    }

    public static Document createPfx02Bid(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), PFX02_BID.getValue()));
        document.selectSingleNode("//PFX02_TAB/@REPO_DATE").setText(data.getOrDefault(REPO_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//FIX/@BID_START_TIME").setText(data.getOrDefault(BID_START_TIME, "14:35:00"));
        document.selectSingleNode("//FIX/@BID_END_TIME").setText(data.getOrDefault(BID_END_TIME, "16:40:00"));
        document.selectSingleNode("//FIX/@FIX_ID").setText(data.getOrDefault(FIX_ID, generateAuctionId(AutomatingBlock.REPO)));
        document.selectSingleNode("//FIX/@FIRST_LEG_DATE").setText(data.getOrDefault(FIRST_LEG_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//FIX/@SECOND_LEG_DATE").setText(data.getOrDefault(SECOND_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue()))));
        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createPfx02BidLrrx(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), PFX02_BID_LRRX.getValue()));
        document.selectSingleNode("//PFX02_TAB/@REPO_DATE").setText(data.getOrDefault(REPO_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//FIX_A/@BID_START_TIME").setText(data.getOrDefault(BID_START_TIME, "14:35:00"));
        document.selectSingleNode("//FIX_A/@BID_END_TIME").setText(data.getOrDefault(BID_END_TIME, "16:40:00"));
        document.selectSingleNode("//FIX_A/@FIX_ID").setText(data.getOrDefault(FIX_ID, generateAuctionId(AutomatingBlock.REPO)));
        document.selectSingleNode("//FIX_A/@FIRST_LEG_DATE").setText(data.getOrDefault(FIRST_LEG_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createPfx02(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), PFX02.getValue()));
        document.selectSingleNode("//PFX02_TAB/@REPO_DATE").setText(data.getOrDefault(REPO_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//AUCTION/@AUCTION_ID").setText(data.getOrDefault(AUCTION_ID, generateAuctionId(AutomatingBlock.REPO)));
        document.selectSingleNode("//AUCTION/@START_TIME").setText(data.getOrDefault(START_TIME, "10:08:00"));
        document.selectSingleNode("//AUCTION/@END_TIME").setText(data.getOrDefault(END_TIME, "10:15:00"));
        document.selectSingleNode("//AUCTION/@FIRST_LEG_DATE").setText(data.getOrDefault(FIRST_LEG_DATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//AUCTION/@SECOND_LEG_DATE").setText(data.getOrDefault(SECOND_LEG_DATE, LocalDateTime.now().plusDays(14).format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//AUCTION/@CURRENCY").setText(data.getOrDefault(CURRENCY, "CNY"));
        document.selectSingleNode("//AUCTION/@RATE_TYPE_AUCT").setText(data.getOrDefault(RATE_TYPE_AUCT, "FIX"));
        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createPfx09(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), PFX09.getValue()));
        document.selectSingleNode("//PFX09_TAB/@LIMITDATE").setText(data.getOrDefault(LIMITDATE, LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode("//PFX09_TAB/@INFOTYPE").setText(data.getOrDefault(INFOTYPE, "1"));
        document.selectSingleNode("//PFX09_REC/@LIMIT").setText(data.getOrDefault(LIMIT, "3000000000.1"));
        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createRepoLoFile(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), REPO_LO_AUCTION.getValue()));
        String selectionInfoRec = "//selection_info_rec/@";
        document.selectSingleNode(selectionInfoRec + SELECTION_DATE.getValue()).setText(data.getOrDefault(SELECTION_DATE, LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + SELECTION_ID.getValue()).setText(data.getOrDefault(SELECTION_ID, generateAuctionId(AutomatingBlock.REPO_LO)));

        document.selectSingleNode(selectionInfoRec + FINSTR.getValue()).setText(data.getOrDefault(FINSTR, "LORA002RS0"));
        document.selectSingleNode(selectionInfoRec + PERIOD.getValue()).setText(data.getOrDefault(PERIOD, "2"));

        document.selectSingleNode(selectionInfoRec + PAYING_DATE.getValue()).setText(data.getOrDefault(PAYING_DATE, LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + REPAYM_DATE.getValue()).setText(data.getOrDefault(REPAYM_DATE, LocalDate.now().plusDays(2).format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + BOOKING_TIME_START.getValue()).setText(data.getOrDefault(BOOKING_TIME_START, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + BOOKING_TIME_FINISH.getValue()).setText(data.getOrDefault(BOOKING_TIME_FINISH, LocalTime.now().plusMinutes(7).format(ofPattern(XML_TIME_FORMAT.getValue()))));

        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createRepoSpbFile(String fileName, Map<XmlAttribute, String> data) {
        Document document = XmlUtils.parseXml(String.format(XML_FILE_TEMPLATE.getValue(), REPO_SPB_AUCTION.getValue()));
        String attr = "/@";
        String docProps = "//doc_props" + attr;
        String selectionInfoRec = "//selection_info_rec" + attr;
        document.selectSingleNode(docProps + DOC_DATE.getValue()).setText(data.getOrDefault(DOC_DATE, "2024-03-28"));
        document.selectSingleNode(docProps + DOC_TIME.getValue()).setText(data.getOrDefault(DOC_TIME, "09:05:00"));
        document.selectSingleNode(docProps + DOC_NAME.getValue()).setText(data.getOrDefault(DOC_NAME, "selection_info_20240328_090500.xml"));
        document.selectSingleNode(docProps + SENDER_DOC_ID.getValue()).setText(data.getOrDefault(SENDER_DOC_ID, "SI_2024032801"));


        document.selectSingleNode(selectionInfoRec + SELECTION_DATE.getValue()).setText(data.getOrDefault(SELECTION_DATE, LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + SELECTION_ID.getValue()).setText(data.getOrDefault(SELECTION_ID, generateAuctionId(AutomatingBlock.REPO_LO)));

        document.selectSingleNode(selectionInfoRec + PERIOD.getValue()).setText(data.getOrDefault(PERIOD, "2"));
        document.selectSingleNode(selectionInfoRec + FINSTR.getValue()).setText(data.getOrDefault(FINSTR, "SPRA002RS1"));
        document.selectSingleNode(selectionInfoRec + TOTAL_SUM.getValue()).setText(data.getOrDefault(TOTAL_SUM, "10000"));
        document.selectSingleNode(selectionInfoRec + MIN_ORD_RATE.getValue()).setText(data.getOrDefault(MIN_ORD_RATE, "10"));
        document.selectSingleNode(selectionInfoRec + RATE_TYPE.getValue()).setText(data.getOrDefault(RATE_TYPE, "FIX"));
        document.selectSingleNode(selectionInfoRec + BENCHMARK.getValue()).setText(data.getOrDefault(BENCHMARK, ""));


        if (data.containsKey(FIX_DISCOUNT))
            ((Element) document.selectSingleNode(selectionInfoRec.substring(0, selectionInfoRec.indexOf(attr)))).addAttribute(FIX_DISCOUNT.getValue(), data.get(FIX_DISCOUNT));

        document.selectSingleNode(selectionInfoRec + PAYING_DATE.getValue()).setText(data.getOrDefault(PAYING_DATE, LocalDate.now().plusDays(1).format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + REPAYM_DATE.getValue()).setText(data.getOrDefault(REPAYM_DATE, LocalDate.now().plusDays(8).format(ofPattern(XML_DATE_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + BOOKING_TIME_START.getValue()).setText(data.getOrDefault(BOOKING_TIME_START, LocalTime.now().plusMinutes(2).format(ofPattern(XML_TIME_FORMAT.getValue()))));
        document.selectSingleNode(selectionInfoRec + BOOKING_TIME_FINISH.getValue()).setText(data.getOrDefault(BOOKING_TIME_FINISH, LocalTime.now().plusMinutes(7).format(ofPattern(XML_TIME_FORMAT.getValue()))));

        log.info("Создать {} файл", "target/temp/" + fileName);
        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static Document createMkrSecExport(String fileName) {
        SpvbUtils.step("Создать .xml файл с сформированными уникальными SECURITY ID");
        String xml = """
                <?xml version="1.0" encoding="WINDOWS-1251"?>
                <SPCEX_DOC>
                </SPCEX_DOC>
                """;

        Document document = XmlUtils.parseStringToDocument(xml);
        Element el = (Element) document.selectSingleNode("SPCEX_DOC");
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DVEB10S%s"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DSTAA10S%s"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DT1100K%sU"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DK2000K%sU"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DM1000S%s"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DG1000S%s"));
        el.addElement("SECURITY").addAttribute("ID", generateSecCode("DL1000S0%s"));

        return XmlUtils.toXml(document, TEMP_FILES.getValue() + fileName);
    }

    public static String generateAuctionId(AutomatingBlock block) {
        List<String> idList = null;
        int length = 6;
        switch (block) {
            case REPO -> idList = new AuctionDbHelper().getAuctionRepoIdList().stream()
                    .map(AuctionDbo::getId)
                    .toList();
            case BR_OFZ -> idList = new AuctionDbHelper().getAuctionIdList().stream()
                    .map(AuctionDbo::getId)
                    .toList();
            case REPO_LO -> {
                idList = new OverCountRepoDbHelper()
                        .getIdList();
                length = 5;
            }
        }

        String id = RandomUtils.getRandomNumWithLength(length);
        while (idList.contains(id)) {
            id = RandomUtils.getRandomNumWithLength(length);
        }
        return id;
    }

    public static String generateSecCode(String secCodeTemplate) {
        List<String> secCodes = new MkrDbHelper().getSecCodeList(String.format(secCodeTemplate, "%%"))
                .stream().map(a -> a.secCode).toList();
        String code = String.format(secCodeTemplate, RandomUtils.getRandomNumWithLength(3));
        while (secCodes.contains(code)) {
            code = String.format(secCodeTemplate, RandomUtils.getRandomNumWithLength(3));
        }
        return code;
    }
}
