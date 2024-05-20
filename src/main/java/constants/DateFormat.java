package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DateFormat {

    SFTP_DATE_FORMAT1("ddMMyyyy"),
    SFTP_DATE_FORMAT2("yyyyMMdd"),
    PROCESSING_APP_TIME_FORMAT("HH:mm"),
    WEB_DATE_FORMAT("dd.MM.yyyy"),
    WEB_DATE_TIME_FORMAT("dd.MM.yyyy HH:mm:ss"),
    WEB_DATE_TIME_FORMAT_NO_MINUTES("dd.MM.yyyy HH"),
    DB_DATE_TIME_FORMAT("yyyy-MM-dd HH:mm:ss"),
    AUDIT_DATE_TIME_FORMAT("dd.MM.yyyy HH:mm"),
    FILE_DATE_FORMAT("ddMMyy"),
    XML_DATE_FORMAT("yyyy-MM-dd"),
    XML_TIME_FORMAT("HH:mm:ss");

    private final String value;
}
