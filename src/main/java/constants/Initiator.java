package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Initiator{
    LO("LO", "КФ ЛО"),
    SPB("SP", "Комитет финансов СПб");
    private final String dbCode;
    private final String initiatorName;
}
