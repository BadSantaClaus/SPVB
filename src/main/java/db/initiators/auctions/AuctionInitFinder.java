package db.initiators.auctions;

import constants.Extension;
import constants.Initiator;
import db.SqlQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static constants.DateFormat.DB_DATE_TIME_FORMAT;
import static constants.DateFormat.XML_DATE_FORMAT;
import static java.time.format.DateTimeFormatter.ofPattern;

public class AuctionInitFinder {
    private final StringBuilder query = new StringBuilder();

    public AuctionInitFinder(List<String> columns) {
        query.append("select ");
        for (int i = 0; i < columns.size() - 1; i++) {
            query.append(columns.get(i)).append(", ");
        }
        query.append(columns.get(columns.size() - 1)).append("\n");

    }

    public AuctionInitFinder fromDocument() {
        query.append("from initiators.document d\n");
        return this;
    }

    public AuctionInitFinder fromDocumentAndAuction() {
        query.append("from initiators.document d join initiators.auction a on d.id = a.document_id\n");
        return this;
    }

    public AuctionInitFinder where() {
        query.append(" where true \n");
        return this;
    }

    public AuctionInitFinder withDNameLike(String template) {
        query.append(" and d.name like '").append(template).append("'\n");
        return this;
    }

    public AuctionInitFinder withDFormatType(Extension extension) {
        query.append("and d.format_type = '")
                .append(extension.getValue().substring(1).toUpperCase())
                .append("'\n");
        return this;
    }

    public AuctionInitFinder withDInitiatorCode(Initiator initiator) {
        query.append("and d.initiator_code = '")
                .append(initiator.getDbCode())
                .append("'\n");
        return this;
    }

    public AuctionInitFinder withAAuctionType(String auctionType) {
        query.append("and a.auction_type = '")
                .append(auctionType)
                .append("'\n");
        return this;
    }

    public AuctionInitFinder withADepositContractId(String biCode){
        query.append("and a.deposit_contract_id = '")
                .append(biCode)
                .append("'\n");
        return this;
    }

    public AuctionInitFinder withAStatus(String status){
        query.append("and a.status = '")
                .append(status)
                .append("'\n");
        return this;
    }

    public AuctionInitFinder withACreatedDateBetween(LocalDate from, LocalDate to) {
        String template = "and a.created_date between '%s' and '%s'\n";
        query.append(String.format(template,
                from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                to.format(ofPattern(XML_DATE_FORMAT.getValue()))
        ));
        return this;
    }

    public AuctionInitFinder withDCreatedDateBetween(LocalDateTime from, LocalDateTime to){
        String template = "and d.created_date between '%s' and '%s'\n";
        query.append(String.format(template,
                from.format(ofPattern(DB_DATE_TIME_FORMAT.getValue())),
                to.format(ofPattern(DB_DATE_TIME_FORMAT.getValue()))
        ));
        return this;
    }

    public AuctionInitFinder orderBy(String column, boolean isDesc) {
        query.append("order by ").append(column);
        if (isDesc)
            query.append(" desc");
        return this;
    }

    public SqlQuery build() {
        return new SqlQuery(query.toString(), new Object[]{});
    }
}
