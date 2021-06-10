package manchiro.manchirorin;

import java.util.Arrays;

public enum MCHRole {
    JACKPOT("ピンゾロ", (one, two, three) -> {
        if (one == two && two == three && one == 1) return 1;
        return -1;
    }),
    MAN5RORIN("オマンコロ", (one, two, three) -> {
        if (one == two && two == three && one == 6) return 6;
        return -1;
    }),
    DOUBLET("ゾロメ", (one, two, three) -> {
        if (one == two && two == three) return one;
        return -1;
    }),
    HIFUMI("ヒフミ", (one, two, three) -> {
        int i = 0;
        for (int j = 1; j < 4; j++) {
            if (one == j || two == j || three == j) i++;
        }
        if (i == 3) return 123;
        return -1;
    }),
    SAIKO("サイコー", (one, two, three) -> {
        if (one == 3 && two == 1 && three == 5) return 315;
        return -1;
    }),
    MAN10("man10", (one, two, three) -> {
        if (one + two + three == 10) return 10;
        return -1;
    }),
    DAN5("dan5", (one, two, three) -> {
        if (one + two + three == 5) return 5;
        return -1;
    }),
    ONE("イチ", 1),
    TWO("ニ", 2),
    THREE("サン", 3),
    FOUR("シ", 4),
    FIVE("ゴ", 5),
    SIX("ロ", 6),
    NONE("ナシ", (one, two, three) -> 0);

    public final String name;
    public final int role;
    private final RoleFunction function;

    MCHRole(String name, RoleFunction function) {
        this(name, -1, function);
    }
    MCHRole(String name, int role) {
        this(name, role, (one, two, three) -> {
            if (one == two) return three == role ? role : -1;
            if (two == three) return one == role ? role : -1;
            if (three == one) return two == role ? role : -1;
            return -1;
        });
    }
    MCHRole(String name, int role, RoleFunction function) {
        this.name = name;
        this.role = role;
        this.function = function;
    }

    public boolean isNumberRole() {
        return 6 >= role && role >= 1;
    }

    public static MCHRole getRole(int one, int two, int three) {
        for (MCHRole role : Arrays.copyOf(values(), values().length - 1)) {
            int result = role.function.accept(one, two, three);
            if (result == -1) continue;
            return role;
        }
        return NONE;
    }

    @Override
    public String toString() {
        return name;
    }

    private interface RoleFunction {
        int accept(int one, int two, int three);
    }
}
