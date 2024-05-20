package utils;

import constants.Credentials;
import constants.DateFormat;
import db.initiators.limits.LimitsDbHelper;
import model.dto.LimitDto;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LimUtils {
    public static List<LimitDto> fileToLimitsDto(File file){
        List<LimitDto> dtoList = new ArrayList<>();
        Assertions.assertTrue(SpvbUtils.getFileExtension(file.getAbsolutePath()).equals("lim"),
                "Полученный файл не является файлом типа lim");
        try (Scanner scanner = new Scanner(file)){
            while(scanner.hasNextLine()){
                dtoList.add(new LimitDto(scanner.nextLine()));
            }
        }
        catch (FileNotFoundException fileNotFoundException){
            System.out.println(fileNotFoundException.getStackTrace());
        }
        return dtoList;
    }

    public static String generateName(String uploadType, String initCode, String type){
        String name = uploadType.toUpperCase() + '_' +
                initCode.toUpperCase() + "_LIMITS_" +
                LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())) + '_';
        String index = String.valueOf(Integer.parseInt(new LimitsDbHelper(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(),
                Credentials.getInstance().dbPassword()).getLastIndex(type.toLowerCase())) + 1);
        while(index.length() != 3){
            index = '0' + index;
        }

        return name + index + '.' + type.toLowerCase();
    }
}
