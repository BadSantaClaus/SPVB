package ru.spvb.initiators;

import constants.Credentials;
import constants.DocStatus;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import page.web.initiators.auction.DepositAuctionNTradesPage;
import page.web.initiators.limits.OpenedLimitPage;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.auctionInitSteps.AuctionInitSteps;
import ru.spvb.steps.limit.LimitSteps;
import utils.WaitingUtils;

import java.io.File;
import java.time.LocalDateTime;

import static ru.spvb.steps.limit.LimitSteps.getLastName;

@Order(4)
@Epic("Проверки после операционного дня")
public class EndOfDayTest extends BaseTest {
    @Test
    @Order(0)
    @Link(name = "SPB-T326", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T326")
    @DisplayName("Проверка автоотклонения аукционов по завершении операционного дня ///20:10 - 20:20")
    public void t326() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(20).withMinute(10));
        SoftAssertions softly = new SoftAssertions();
        new AuctionInitSteps()
                .checkAucDeclined("DSTAA10S035", DepositAuctionNTradesPage.UiAuctionsNTradeSection.STANDARD, softly)
                .checkAucDeclined("DVEB10S010", DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF, softly)
                .checkNewAucNotExist(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB, softly)
                .checkAucDeclined("DL1000S0017", DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO, softly)
                .checkAucDeclined("DK1000K023R", DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK, softly);
        softly.assertAll();
    }

    @Test
    @Order(1)
    @Link(name = "SPB-T284", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T284")
    @DisplayName("SPB-T284. Негативный. Проверка экспорта лимитов в рамках периодах после окончания Операционного дня")
    public void SPB_T284Test(){
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/REPO_SP_LIMITS_11012024_007(ТК_2.2).xml");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadREPO("Комитет финансов СПб", file)
                .openLimitPage(getLastName("xml"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .exportLimits()
                .timeErrorLate();
    }
}
