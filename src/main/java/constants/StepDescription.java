package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StepDescription {

    PREPARE_DATA("Подготовка данных");

    private final String value;
}
