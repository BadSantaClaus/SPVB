package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StepName {

    PFX11_VALIDATION_NOTIFICATION("PFX11: Валидация и проверка бизнес-логики Уведомления о приеме электронного документа"),
    IMPORT_UT_FROM_QUIK("Импорт заявок УТ из QUIK"),
    IMPORT_REPO_FROM_QUIK("Импорт сделок РЕПО из QUIK"),
    WAIT_PFX12("Ожидание PFX12"),
    PFX12_SEND_NOTIFICATION_TO_BR("PFX12: Отправка Уведомления о приеме электронного документа в БР"),
    PFX09_VALIDATION("PFX09: Валидация и проверка бизнес-логики документа PFX09 (%s)"),
    PFX09_EXPORT_TO_QUIK("PFX09: Экспорт в QUIK данных об установлении лимитов по операциям РЕПО в рублях (%s)");

    private final String value;
}
