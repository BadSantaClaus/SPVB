package model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class YTDto implements Comparable{
    private String name = null;
    private String code;
    private String generalLimit;
    private List<List<String>> termLimits;

    @Override
    public int compareTo(Object o) {
        YTDto ytDto = (YTDto) o;
        return this.code.compareTo(ytDto.getCode());
    }

    public YTDto (String code, String generalLimit, List<List<String>> termLimits){
        this.code = code;
        this.generalLimit = generalLimit;
        this.termLimits = termLimits;
    }

    @Override
    public boolean equals(Object obj) {
        YTDto ytDto = (YTDto) obj;
        return this.code.equals(ytDto.getCode()) &&
                ((name == null || ytDto.getName() == null) || this.name.equals(ytDto.getName())) &&
                this.generalLimit.equals(ytDto.getGeneralLimit()) &&
                (this.termLimits.containsAll(ytDto.getTermLimits()) && ytDto.getTermLimits().containsAll(this.termLimits));
    }

    @Override
    public String toString() {
        return "YTDto{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", generalLimit='" + generalLimit + '\'' +
                ", termLimits=" + termLimits +
                '}';
    }
}
