package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CmdCommand {

    UNDERWRITE_FILE ("cryptcp -sign -dn \"br_cert\" %s -norev"),
    UNDERWRITE_ZPFX("cryptcp -sign -dn \"br_bank_cert\" %s -norev"),
    CLOSE_FLANIUM_DRIVER ("taskkill /IM FlaNium.Driver.exe /F"),
    CLOSE_QUIK ("taskkill /IM info.exe /F");

    private final String value;
}
