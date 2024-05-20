package db.initiators.limits;

import db.DbHelper;
import db.SqlQuery;
import model.dbo.LimitsDbo;
import model.dto.YTDto;
import utils.WaitingUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static constants.DateFormat.XML_DATE_FORMAT;

public class LimitsDbHelper extends DbHelper {
    public LimitsDbHelper (String url, String login, String password){
        super(url, login, password);
    }


    public String getLastName(String fileType){
        WaitingUtils.sleep(4);
        SqlQuery sqlQuery = new SqlQuery("SELECT name\n" +
                "FROM initiators.document\n" +
                "WHERE document_date = '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(XML_DATE_FORMAT.getValue())) +
                "' AND format_type = '" + fileType.toUpperCase() + "' AND \n" +
                "type ='LIMITS'\n" +
                "ORDER BY number DESC\n" +
                "LIMIT 1", new Object[]{});
        return this.queryList(LimitsDbo.class, sqlQuery).get(0).name;
    }

    public String getLastIndex(String fileType){
        WaitingUtils.sleep(4);
        SqlQuery sqlQuery = new SqlQuery("SELECT number\n" +
                "FROM initiators.document\n" +
                "WHERE document_date = '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(XML_DATE_FORMAT.getValue())) +
                "' AND format_type = '" + fileType.toUpperCase() + "' AND \n" +
                "type ='LIMITS'\n" +
                "ORDER BY number DESC\n" +
                "LIMIT 1", new Object[]{});
        try {
            return this.queryList(LimitsDbo.class, sqlQuery).get(0).number;
        }
        catch (IndexOutOfBoundsException e){
            return "0";
        }
    }

    public String QExportCheck(String tag, List<YTDto> list){
        WaitingUtils.sleep(4);
        String codes = "";
        for (YTDto ytDto : list){
            codes += '\'' + ytDto.getCode() + "', ";
        }
        codes = codes.substring(0, codes.length() - 2);
        SqlQuery sqlQuery = new SqlQuery("SELECT COUNT(*) AS 'count' FROM MoneyLimits\n" +
                "WHERE Tag = '" + tag + "' AND FirmID IN (" + codes + ")", new Object[]{});
        return this.queryList(LimitsDbo.class, sqlQuery).get(0).count;
    }
}
