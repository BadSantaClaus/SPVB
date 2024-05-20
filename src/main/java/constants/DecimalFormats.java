package constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DecimalFormats {

    BIG_DOUBLE("###.00"),
    BIG_DOUBLE_WITH_SPACE("###,###.00");
    private final String value;
}
