package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePath {

    TEMP_FILES("target/temp/"),
    TEMP_FILES_FORMAT(TEMP_FILES.value + "%s"),
    FILE_TEMPLATES_DIR("src/test/resources/file_templates/"),
    XML_FILE_TEMPLATE(FILE_TEMPLATES_DIR.value + "%s.xml"),
    BR_IN("br/in/"),
    BR_OUT("br/out/"),
    BR_OUT_NO_SIGN("out/work/br/"),
    DOWNLOAD_FOLDER("target/downloads"),
    REPORT_USERS_OUT("report/users/out/"),
    INITIATORS_MKR_SECURITIES("initiators/mkr/securities/"),
    FLANIUM("src/main/resources/driver/flanium_2.2.1/FlaNium.Driver.exe"),
    QUIK_FAU_GGE("C:\\Users\\bell\\Quik\\QUIK UID--575 ----113   -- ФАУ ГГЭ\\info.exe"),
    QUIK_SBER("C:\\Users\\bell\\Quik\\QUIK UID36=====Сбер\\QUIK UID36=====Сбер\\info.exe"),
    QUIK_AB_RUSSIA("C:\\Users\\bell\\Quik\\QUIK UID35 ====АКБ Россия\\info.exe"),
    QUIK_VTB("C:\\Users\\bell\\Quik\\QUIK UID120 ====ВТБ\\QUIK UID120 ====ВТБ\\info.exe"),
    QUIK_FINANCE_SPB("C:\\Users\\bell\\Quik\\QUIK UID---         302 ---- Комитет Финансов СПБ\\QUIK UID---         302 ---- Комитет Финансов СПБ\\info.exe"),
    QUIK_FSKMB("C:\\Users\\bell\\Quik\\QUIK UID--495 ----112   -- ФСКМБ\\QUIK UID--495 ----112   -- ФСКМБ\\info.exe"),
    QUIK_FK("C:\\Users\\bell\\Quik\\QUIK UID550 K ----K-----ФК\\QUIK UID550 K ----K-----ФК\\info.exe"),
    QUIK_FINANCE_LO("C:\\Users\\bell\\Quik\\QUIK UID--574  ------118-- Комитет финансов ЛО\\QUIK UID--574  ------118-- Комитет финансов ЛО\\info.exe"),
    QUIK_VEB("C:\\Users\\bell\\Quik\\QUIK UID-----594-------- VEB\\QUIK UID-----594-------- VEB\\info.exe"),
    QUIK_CB("C:\\Users\\bell\\Quik\\QUIK ЦБ\\QUIK ЦБ\\info.exe"),
    QUIK_PSB("C:\\Users\\bell\\Quik\\QUIK PSb 472\\QUIK PSb 472\\info.exe"),
    BR_LIM("src/test/resources/lim_files/REPO BR Lim PSB.lim"),
    RKFL_LIM("src/test/resources/lim_files/RKFL.lim"),
    AUCTION_LIMITS_LIM("src/test/resources/limits/auction_limits.lim"),
    QUIK_SMOLNOGO("C:\\Users\\bell\\Quik\\QUIK UID589 AA SMOLNY\\info.exe");


    private final String value;
}