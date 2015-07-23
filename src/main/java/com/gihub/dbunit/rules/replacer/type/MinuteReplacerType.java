package com.gihub.dbunit.rules.replacer.type;

/**
 * Created by pestano on 22/07/15.
 */
public enum MinuteReplacerType implements ReplacerType {
    NOW(0),
    PLUS_ONE(1),
    MINUS_ONE(-1),
    PLUS_TEN(+10),
    MINUS_TEN(-10),
    PLUS_30(+30),
    MINUS_30(-30);

    MinuteReplacerType(int minutes) {
        this.minutes = minutes;
    }

    private final int minutes;

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String getPerfix() {
        return "MIN";
    }

    @Override
    public String getName() {
        return name();
    }

}
