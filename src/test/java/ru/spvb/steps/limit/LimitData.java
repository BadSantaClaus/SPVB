package ru.spvb.steps.limit;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.SneakyThrows;
import model.dto.YTDto;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

//поставить суммы 10 000 000 000 000
public class LimitData {
    public static List<YTDto> originalDataTest1_1(){
        List<List<String>> termLimits = new ArrayList<>();
        List<String> termLimit1 = new ArrayList<>();
        termLimit1.add("1");
        termLimit1.add("1000000000000");
        List<String> termLimit2 = new ArrayList<>();
        termLimit2.add("2");
        termLimit2.add("1000000000000");
        termLimits.add(termLimit1);
        termLimits.add(termLimit2);
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АО \"АБ \"РОССИЯ\"", "45","1000000000000", termLimits));
        list.add(new YTDto("ПАО \"Сбербанк\"", "8", "1000000000000", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest1_2(){
        List<List<String>> termLimits = new ArrayList<>();
        List<String> termLimit1 = new ArrayList<>();
        termLimit1.add("2");
        termLimit1.add("1000000000000");
        List<String> termLimit2 = new ArrayList<>();
        termLimit2.add("11");
        termLimit2.add("1000000000000");
        List<String> termLimit3 = new ArrayList<>();
        termLimit3.add("25");
        termLimit3.add("1000000000000");
        List<String> termLimit4 = new ArrayList<>();
        termLimit4.add("46");
        termLimit4.add("1000000000000");
        List<String> termLimit5 = new ArrayList<>();
        termLimit5.add("76");
        termLimit5.add("1000000000000");
        termLimits.add(termLimit1);
        termLimits.add(termLimit2);
        termLimits.add(termLimit3);
        termLimits.add(termLimit4);
        termLimits.add(termLimit5);
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АО АКБ \"НОВИКОМБАНК\"", "585","1000000000000", termLimits));
        list.add(new YTDto("ПАО \"МОСКОВСКИЙ КРЕДИТНЫЙ БАНК\"", "421", "1000000000000", termLimits));
        list.add(new YTDto("АО \"АБ \"РОССИЯ\"", "45", "1000000000000", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest1_3(){
        List<List<String>> termLimits = new ArrayList<>();
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("ПАО \"Промсвязьбанк\"", "472","999999999999", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest1_4(){
        List<List<String>> termLimits = new ArrayList<>();
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АО \"Почта Банк\"", 	"587","12345678901", termLimits));
        list.add(new YTDto("ПАО \"Промсвязьбанк\"", "472", "5555555555", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest1_6(){
        List<List<String>> termLimits = new ArrayList<>();
        List<String> termLimit1 = new ArrayList<>();
        termLimit1.add("30");
        termLimit1.add("1000000000000");
        List<String> termLimit2 = new ArrayList<>();
        termLimit2.add("90");
        termLimit2.add("1000000000000");
        List<String> termLimit3 = new ArrayList<>();
        termLimit3.add("180");
        termLimit3.add("1000000000000");
        List<String> termLimit4 = new ArrayList<>();
        termLimit4.add("360");
        termLimit4.add("1000000000000");
        termLimits.add(termLimit1);
        termLimits.add(termLimit2);
        termLimits.add(termLimit3);
        termLimits.add(termLimit4);
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("Банк ВТБ (ПАО)", "095","1000000000000", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest2_1(){
        List<List<String>> termLimits = new ArrayList<>();
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АО \"АБ \"РОССИЯ\"", "45", "1000000000", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest2_2(){
        List<List<String>> termLimits = new ArrayList<>();
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АКБ \"ПЕРЕСВЕТ\" (ПАО)", "506", "111111111111000000", termLimits));
        list.add(new YTDto("АО \"Почта Банк\"", "587", "111111111111000000", termLimits));
        list.add(new YTDto("ПАО \"Промсвязьбанк\"", "472", "3785983459000000", termLimits));
        return list;
    }

    public static List<YTDto> originalDataTest3_1(){
        List<List<String>> termLimits = new ArrayList<>();
        List<String> termLimit1 = new ArrayList<>();
        termLimit1.add("2");
        termLimit1.add("500000");
        List<String> termLimit2 = new ArrayList<>();
        termLimit2.add("11");
        termLimit2.add("5");
        List<String> termLimit3 = new ArrayList<>();
        termLimit3.add("25");
        termLimit3.add("5");
        List<String> termLimit4 = new ArrayList<>();
        termLimit4.add("46");
        termLimit4.add("0");
        List<String> termLimit5 = new ArrayList<>();
        termLimit5.add("76");
        termLimit5.add("0");
        termLimits.add(termLimit1);
        termLimits.add(termLimit2);
        termLimits.add(termLimit3);
        termLimits.add(termLimit4);
        termLimits.add(termLimit5);
        List<YTDto> list = new ArrayList<>();
        list.add(new YTDto("АО \"АБ \"РОССИЯ\"", "45", "100000000000", termLimits));
        return list;
    }

    @SneakyThrows
    public static List<YTDto> originalDataTest3_3(){
        List<YTDto> list = new ArrayList<>();
        File file = new File("src/test/resources/Limits/МНОГО ЛИМИТОВ - T(ТК_3.3).dbf");
        DBFReader reader = new DBFReader(new FileInputStream(file));
        List<String> terms = new ArrayList<>();
        terms.add("2");
        terms.add("11");
        terms.add("25");
        terms.add("46");
        terms.add("76");
        for (int i = 0; i < 18; i++){
            List<List<String>> termLimits = new ArrayList<>();
            DBFRow row = reader.nextRow();
            String code = row.getString(2);
            if (code.equals("4")){continue;}
            String generalLimit = row.getString(5);
            for (int j = 0; j < 5; j++){
                List<String> termLimit = new ArrayList<>();
                termLimit.add(terms.get(j));
                termLimit.add(row.getString(6 + j));
                termLimits.add(termLimit);
            }
            list.add(new YTDto(code, generalLimit, termLimits));
        }
        return list;
    }
}
