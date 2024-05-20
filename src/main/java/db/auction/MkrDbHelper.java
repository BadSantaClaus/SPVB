package db.auction;

import constants.Credentials;
import db.DbHelper;
import model.dbo.MkrDbo;

import java.util.List;

public class MkrDbHelper extends DbHelper {

    public MkrDbHelper() {
        super(Credentials.getInstance().dbArkUrl(), Credentials.getInstance().dbArkLogin(), Credentials.getInstance().dbArkPassword());
    }

    public List<MkrDbo> getSecCodeList(String likeSecCode) {
        String query = String.format("SELECT SecCode FROM MKR.dbo.Securities s WHERE SecCode LIKE '%s'", likeSecCode);
        return queryList(query, MkrDbo.class);
    }

    public List<String> getDistinctSecCodes(List<String> instruments) {
        StringBuilder sb = new StringBuilder();
        for (String str : instruments) {
            sb.append("'").append(str).append("', ");
        }
        sb.replace(sb.length() - 2, sb.length() - 1, "");
        String query = "SELECT DISTINCT SecCode FROM MKR.dbo.Securities s WHERE SecCode IN (%s)";
        return queryList(String.format(query,sb), MkrDbo.class).stream().map(a->a.secCode).toList();
    }

    public void deleteInstrument(String secCode){
        String queryGetClassCode = """
                SELECT c.ClassCode
                FROM MKR.dbo.Securities s
                JOIN MKR.dbo.Classes c ON s.ClassID = c.ClassID
                WHERE s.SecCode IN ('%s')
                """.formatted(secCode);
        List<String> classCodes = queryList(queryGetClassCode, MkrDbo.class).stream().map(a->a.classCode).toList();
        for(String code : classCodes) {
            new DbHelper(Credentials.getInstance().dbArkUrl(), Credentials.getInstance().dbArkLogin(), Credentials.getInstance().dbArkPassword())
                    .exec(String.format("EXEC MKR.dbo.removeSecurity '%s' , '%s'", code, secCode));
        }

    }
}
