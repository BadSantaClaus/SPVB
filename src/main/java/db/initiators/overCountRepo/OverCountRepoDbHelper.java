package db.initiators.overCountRepo;

import com.codeborne.selenide.Selenide;
import constants.Credentials;
import constants.Extension;
import constants.Initiator;
import db.DbHelper;
import db.SqlQuery;
import db.initiators.auctions.AuctionInitFinder;
import model.dbo.LimitsDbo;
import model.dbo.OverCountRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OverCountRepoDbHelper extends DbHelper {

    public OverCountRepoDbHelper(){
        super(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(), Credentials.getInstance().dbPassword());
    }

    public String getLastName(Initiator initiator, String biCode) {
        SqlQuery sqlQuery = new AuctionInitFinder(List.of("d.name"))
                .fromDocumentAndAuction()
                .where()
                .withDCreatedDateBetween(
                        LocalDateTime.now().minusMinutes(5),
                        LocalDateTime.now().plusMinutes(5)
                ).withDFormatType(Extension.XML)
                .withDInitiatorCode(initiator)
                .withADepositContractId(biCode)
                .withAStatus("NEW")
                .orderBy("d.number", true)
                .build();
        for (int i = 0; i < 10; i++) {
            try {
                return this.queryList(LimitsDbo.class, sqlQuery).get(0).name;
            }catch (IndexOutOfBoundsException e){
                Selenide.sleep(15000);
            }
        }
        throw new RuntimeException("Аукционы не найдены");
    }

    public String getLastNameAfterTime(Initiator initiator, LocalDateTime after) {
        SqlQuery sqlQuery = new AuctionInitFinder(List.of("d.name"))
                .fromDocument()
                .where()
                .withDCreatedDateBetween(
                        after,
                        LocalDateTime.now().plusMinutes(5)
                ).withDFormatType(Extension.XML)
                .withDInitiatorCode(initiator)
                .orderBy("d.number", true)
                .build();
        for (int i = 0; i < 5; i++) {
            try {
                return this.queryList(LimitsDbo.class, sqlQuery).get(0).name;
            }catch (IndexOutOfBoundsException e){
                Selenide.sleep(5000);
            }
        }
        throw new RuntimeException("Аукционы не найдены");
    }

    public List<OverCountRepo> getAppsBeforeTodayStatuses(){
        SqlQuery sqlQuery = new AuctionInitFinder(List.of("d.name","a.status"))
                .fromDocumentAndAuction()
                .where()
                .withAAuctionType("REPO")
                .withACreatedDateBetween(LocalDate.now().minusDays(7), LocalDate.now().minusDays(1))
                .orderBy("a.created_date", true)
                .build();

        return this.queryList(OverCountRepo.class, sqlQuery);
    }

    public List<String> getIdList(){
        return new ArrayList<>();
    }
}
