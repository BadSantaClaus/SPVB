package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    COMPLETE("Завершен"),
    ERROR("Ошибка"),
    NOT_STARTED("Не начат"),
    IN_PROGRESS("Выполняется"),
    START("Старт"),
    SUCCESS("Успех");

    private final String value;
}
