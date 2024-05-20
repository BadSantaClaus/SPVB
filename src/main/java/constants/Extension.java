package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Extension {

    XML(".xml"),
    XML_P7S(".xml.p7s"),
    P7S(".p7s"),
    TRI(".tri"),
    TRO(".tro"),
    ZIP(".zip"),
    SIG(".sig"),
    DOCX(".docx"),
    PDF(".pdf");

    private final String value;
}
