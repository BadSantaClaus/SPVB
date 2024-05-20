package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MenuTab {

    ROUTINE_OPERATIONS("Модуль регламентных операций"),
    BANK_RUSSIA("Модуль обмена с Банком России"),
    INITIATORS("Модуль обмена с инициаторами"),
    AUDIT("Аудит");

    private final String value;
}
