package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InitDocRegistry implements IHasColumnDescriptor{
    NAME_DOC("Имя документа", ".//td[position() = 1]"),
    TYPE("Тип",  ".//td[position() = 2]"),
    INITIATOR_CONTRIBUTOR_CODE("Код инициатора/вкладчика", ".//td[position() = 3]"),
    DATE_RECEIVE("Дата получения",".//td[position() = 4]"),
    DATE_PROCESSING("Дата обработки", ".//td[position() = 5]"),
    STATUS("Статус",".//td[position() = 6]"),
    ;

    private final String name;
    private final String xPath;
}
