package ch.epfl.javions.aircraft;

public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * @param s string
     * @return the wake turbulence category corresponding to the string given
     * @author Rudolf Yazbeck (SCIPER 360700)
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}
