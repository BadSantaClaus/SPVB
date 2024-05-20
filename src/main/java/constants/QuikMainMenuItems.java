package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuikMainMenuItems {

    UPLOAD_POSITIONS_FROM_FILE("Загрузить позиции из файла...", 1);

    private final String value;
    private final int numFromUp;
}
