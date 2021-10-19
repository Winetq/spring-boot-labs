package aui.swimmer;

public enum SwimmingStyle {
    BUTTERFLY,
    BREASTSTROKE,
    BACKSTROKE,
    FREESTYLE;

    public static SwimmingStyle of(String s) {
        for (SwimmingStyle style : SwimmingStyle.values()) {
            if (s.equals(style.name())) {
                return style;
            }
        }
        throw new IllegalArgumentException();
    }
}

