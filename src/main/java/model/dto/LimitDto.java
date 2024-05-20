package model.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class LimitDto {
    private int FIRM_ID;
    private String TAG;
    private String CURR_CODE;
    private String CLIENT_CODE;
    private String OPEN_BALANCE;
    private double OPEN_LIMIT;
    private int LIMIT_KIND;
    private int LEVERAGE;

    public LimitDto (String data){
        String[] fields = data.split(";");
        fields = Arrays.stream(fields).map(String::trim).toArray(String[]::new);
        FIRM_ID = Integer.parseInt (fields[0].substring(fields[0].indexOf('=') + 2));
        TAG = fields[1].substring(fields[1].indexOf('=') + 2);
        CURR_CODE =fields[2].substring(fields[2].indexOf('=') + 2);
        CLIENT_CODE = fields[3].substring(fields[3].indexOf('=') + 2);
        OPEN_BALANCE = fields[4].substring(fields[4].indexOf('=') + 2);
        OPEN_LIMIT = Double.parseDouble (fields[5].substring(fields[5].indexOf('=') + 2));
        LIMIT_KIND = Integer.parseInt (fields[6].substring(fields[6].indexOf('=') + 2));
        LEVERAGE = Integer.parseInt (fields[7].substring(fields[7].indexOf('=') + 2));
    }

    @Override
    public String toString() {
        return "LimitDto{" +
                "FIRM_ID=" + FIRM_ID +
                ", TAG='" + TAG + '\'' +
                ", CURR_CODE='" + CURR_CODE + '\'' +
                ", CLIENT_CODE=" + CLIENT_CODE +
                ", OPEN_BALANCE=" + OPEN_BALANCE +
                ", OPEN_LIMIT=" + OPEN_LIMIT +
                ", LIMIT_KIND=" + LIMIT_KIND +
                ", LEVERAGE=" + LEVERAGE +
                '}';
    }

    @Override
    public boolean equals(Object obj){
        LimitDto limitDto = (LimitDto) obj;
        return this.FIRM_ID == limitDto.getFIRM_ID() &&
                this.TAG.equals(limitDto.getTAG()) &&
                this.CURR_CODE.equals(limitDto.getCURR_CODE()) &&
                this.CLIENT_CODE.equals(limitDto.getCLIENT_CODE()) &&
                this.OPEN_BALANCE.equals(limitDto.getOPEN_BALANCE()) &&
                this.OPEN_LIMIT == limitDto.getOPEN_LIMIT() &&
                this.LIMIT_KIND == limitDto.getLIMIT_KIND() &&
                this.LEVERAGE == limitDto.getLEVERAGE();
    }
}
