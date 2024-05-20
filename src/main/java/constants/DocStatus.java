package constants;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DocStatus {

    NEW("Новый"),
    IN_WORK("В работе"),
    EXPORTING("Экспортируется в ТС"),
    EXPORTED("Экспортирован в ТС"),
    SEND("Отправлен"),
    FINISHED("Завершен"),
    ERROR("Ошибка"),
    DECLINED("Отклонен"),
    COLLECT_APPS("Сбор заявок"),
    COLLECT_DEALS("Сбор сделок"),
    WAIT("Ожидание"),
    COUNTER_REQUESTS("Встречные заявки"),
    RESULTS("Итоги");

    private final String value;
}