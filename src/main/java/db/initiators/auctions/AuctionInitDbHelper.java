package db.initiators.auctions;

import constants.Credentials;
import db.DbHelper;
import db.SqlQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import model.dbo.LimitsDbo;
import utils.WaitingUtils;

import java.time.LocalDateTime;
import java.util.List;

import static constants.DateFormat.XML_DATE_FORMAT;
import static java.time.format.DateTimeFormatter.ofPattern;

public class AuctionInitDbHelper extends DbHelper {


    public AuctionInitDbHelper() {
        super(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(), Credentials.getInstance().dbPassword());
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        AUCTION("AUCTION"),
        TRADE("DTRADE");
        private final String type;
    }

    public String getLastName(String fileType, Type type) {
        WaitingUtils.sleep(5);
        SqlQuery sqlQuery = new SqlQuery("SELECT name\n" +
                "FROM initiators.document\n" +
                "WHERE document_date = '" + LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue())) +
                "' AND format_type = '" + fileType.toUpperCase() + "' AND \n" +
                "type ='" + type.type + "'\n" +
                "ORDER BY number DESC\n" +
                "LIMIT 1", new Object[]{});
        return this.queryList(LimitsDbo.class, sqlQuery).get(0).name;
    }

    public List<LimitsDbo> getAuctionsWithNameTemplate(String template) {
        SqlQuery sqlQuery = new AuctionInitFinder(List.of(
                "d.name"
        )).fromDocument()
                .where()
                .withDNameLike(template)
                .build();
        return this.queryList(LimitsDbo.class, sqlQuery);
    }

}
