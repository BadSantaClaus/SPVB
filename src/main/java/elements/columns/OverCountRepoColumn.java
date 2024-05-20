package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OverCountRepoColumn implements IHasColumnDescriptor{
    NAME_DOC("Имя документа", ".//td[position() = 1]//button"),
    AUCTION_TRADES("Аукцион/Торги", ".//td[position() = 2]"),
    EXCHANGE_INSTRUMENT("Биржевой инструмент",".//td[position() = 4]"),
    DATE_RECEIVE("Дата получения",".//td[position() = 4]"),
    DATE_PROCESSING("Дата обработки", ".//td[position() = 5]"),
    STATUS("Статус",".//td[position() = 6]"),
    ;

    private final String name;
    private final String xPath;
}
