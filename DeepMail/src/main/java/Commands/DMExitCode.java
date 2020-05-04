package Commands;

import picocli.CommandLine.ExitCode;

/**
 * DeepMail Exit Codes. Picocli eeskujul funktsioonide tagastuskoodid.
 */
public final class DMExitCode {
    public static final int EXITMENU = -1;
    public static final int OK = ExitCode.OK;
    public static final int SOFTWARE = ExitCode.SOFTWARE;
    public static final int USAGE = ExitCode.USAGE;
}

/**
 * Kuna Picocli CommandLine.execute ignoreerib call funktsioone,
 * mis tagastavad mitte-Integer väärtusi, siis pole vist mõtet enumi kasutada
 */
enum DMExitCodeEnum {
    OK(0),
    SOFTWARE(1),
    USAGE(2),
    EXITMENU(-1);

    private int value;

    private DMExitCodeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

