package utils;

import java.util.Random;

public class RandomUtils {
    private static final Random random = new Random();

    public static String getRandomNumWithLength(int length){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }

    public static String getRandomIdWithLength(int length){
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(1,9));
        for (int i = 1; i < length; i++) {
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }
}
