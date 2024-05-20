package elements.columns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditColumn implements IHasColumnDescriptor{

    EVENT_DESCRIPTION("Описание события", ".//td[position() = 1]//*[local-name()='svg']"),
    ID("ID", ".//td[position() = 2]//div/div"),
    SESSION_ID("Session ID", ".//td[position() = 3]//div/div"),
    OPERATION_NAME("Наименование операции/события", ".//td[position() = 4]//div"),
    OPERATION_RESULT("Результат операции", ".//td[position() = 5]//div"),
    MODULE("Модуль", ".//td[position() = 6]"),
    DATE_TIME("Дата и время", ".//td[position() = 7]"),
    IP("IP-адрес", ".//td[position() = 8]"),
    FIO("ФИО",  ".//td[position() = 9]/div/p");

    private final String name;
    private final String xPath;
}
