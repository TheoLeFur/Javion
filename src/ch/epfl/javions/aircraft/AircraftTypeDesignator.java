package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 *
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 * @param string aircraft's pattern
 */
public record AircraftTypeDesignator(String string) {
    private final static Pattern allowedStrings = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * @throws IllegalArgumentException if the given pattern is not a number or a letter and of length 2 to 4 (the
     *                                  empty string is a valid designator)
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches() || string.isEmpty());
    }
}