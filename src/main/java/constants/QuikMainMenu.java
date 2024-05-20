package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuikMainMenu {

    SYSTEM("Система", 7),
    CREATE_WINDOW("Создать окно", 6),
    ACTIONS("Действия", 5),
    BROKER("Брокер", 4),
    EXTENSIONS("Расширения", 3),
    SERVICES("Сервисы",2),
    WINDOWS("Окна",1);

    private final String value;
    private final int numFromLeft;
}
