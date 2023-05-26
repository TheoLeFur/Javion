package ch.epfl.javions.aircraft;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * @param s string that will be interpreted into a turbulence category
     * @return the wake turbulence category corresponding to the string given
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
