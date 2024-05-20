package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoutineOperationsProcessColumns implements IHasColumnDescriptor {

    MODULE("Модуль", ".//td[position() = 1]//div"),
    PROCESS_NAME("Название процесса", ".//td[position() = 2]//div"),
    UUID("UUID", "td/div"),
    PROCESS_CREATION_TIME("Время создания процесса", ".//td[position() = 4]//div"),
    PLANNED_START_TIME("Планируемое время старта", ".//td[position() = 5]"),
    ACTUAL_START_TIME("Фактическое время старта", ".//td[position() = 6]"),
    PLANNED_END_TIME("Планируемое время завершения", ".//td[position() = 7]"),
    ACTUAL_END_TIME("Фактическое время завершения", ".//td[position() = 8]"),
    STATUS("Статус", ".//td[position() = 9]//span"),
    OPERATING_MODE("Режим работы", ".//td[position() = 10]//span");


    private final String name;
    private final String xPath;

}
