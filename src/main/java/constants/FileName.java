package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileName {

    PFX41("PFX41"),
    PFX42("PFX42"),
    PFX42_T113("PFX42_T113"),
    PFX42_T189_1("PFX42_T189_1"),
    PFX42_T189_2("PFX42_T189_2"),
    PFX42_T95_1("PFX42_T95_1"),
    PFX42_T95_2("PFX42_T95_2"),
    PFX42_T197("PFX42_T197"),
    PFX43("PFX43"),
    PFX38("PFX38"),
    PFX39("PFX39"),
    PFX02("PFX02"),
    PFX02_BID("PFX02_BID"),
    PFX02_BID_LRRX("PFX02_BID_LRRX"),
    PFX09("PFX09"),
    PFX12("PFX12"),
    PFX13("PFX13"),
    PFF63("PFF63"),
    ZPFX38("zPFX38"),
    ZPFX11("zPFX11"),
    ZPFX13("zPFX13"),
    ZPFX63("zPFX63"),
    ZPFX43("zPFX43"),
    ZPFF63("zPFF63"),
    REPO_LO_AUCTION("REPO_LO_AUCTION"),
    REPO_SPB_AUCTION("REPO_SPB_AUCTION"),
    MKR_SECURITY_EXPORT("MKR_SECURITY_EXPORT"),
    PFX11("PFX11");


    private final String value;
}
