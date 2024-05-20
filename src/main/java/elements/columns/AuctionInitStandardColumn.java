package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionInitStandardColumn implements IHasColumnDescriptor{

    NAME_DOC("Имя документа", ".//td[position() = 1]//button"),
    AUCTION_TRADES("Аукцион/Торги", ".//td[position() = 2]"),
    INITIATOR_CONTRIBUTOR_CODE("Код инициатора/вкладчика", ".//td[position() = 3]"),
    EXCHANGE_INSTRUMENT("Биржевой инструмент",".//td[position() = 4]"),
    DATE_RECEIVE("Дата получения",".//td[position() = 5]"),
    DATE_PROCESSING("Дата обработки", ".//td[position() = 6]"),
    STATUS("Статус",".//td[position() = 7]"),
    ;

    private final String name;
    private final String xPath;
}
