package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AutomatingBlock {

    BR_OFZ("БР:ОФЗ"),
    REPO("РЕПО"),
    REPO_LO("REPO_LO");

    private final String value;
}
