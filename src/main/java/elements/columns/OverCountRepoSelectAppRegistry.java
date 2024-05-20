package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OverCountRepoSelectAppRegistry implements IHasColumnDescriptor{

    N("№", ".//td[1]"),
    BANK("Банк", ".//td[2]"),
    BANK_CODE("Код Банка",".//td[3]"),
    REPO_RATE("Ставка РЕПО",".//td[4]"),
    APP("Заявка",".//td[5]"),
    SUM_REPO("Сумма РЕПО в валюте расчетов",".//td[6]");

    private final String name;
    private final String xPath;
}
