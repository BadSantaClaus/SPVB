package db.auction;

import constants.Credentials;
import db.DbHelper;
import model.dbo.AuctionDbo;

import java.util.List;

public class AuctionDbHelper extends DbHelper {

    public AuctionDbHelper() {
        super(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(), Credentials.getInstance().dbPassword());
    }

    public List<AuctionDbo> getAuctionIdList() {
        String query = "SELECT a.id " +
                "FROM rbankexchange.auction a";
        return queryList(query, AuctionDbo.class);
    }

    public List<AuctionDbo> getAuctionRepoIdList(){
        String query = "SELECT a.id " +
                "FROM rbankexchange.auction_repo a";
        return queryList(query, AuctionDbo.class);    }
}
