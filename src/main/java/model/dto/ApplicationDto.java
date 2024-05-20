package model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ApplicationDto {
    private String bankName;
    private String percent;
    private String sum;

    public ApplicationDto(String bankName, String percent, String sum) {
        this.bankName = bankName;
        if(percent.contains(".")){this.percent = percent;}
        else {this.percent = percent + ".00";}
        if(sum.contains(".")){this.sum = sum;}
        else {this.sum = sum + "000.00";}
        if (this.sum.contains(" ")){this.sum = this.sum.replaceAll(" ", "");}
    }

    @Override
    public String toString() {
        return "ApplicationDto{" +
                "bankName='" + bankName + '\'' +
                ", percent='" + percent + '\'' +
                ", sum='" + sum + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationDto that = (ApplicationDto) o;
        return Objects.equals(bankName, that.bankName) && Objects.equals(percent, that.percent) && Objects.equals(sum, that.sum);
    }
}
