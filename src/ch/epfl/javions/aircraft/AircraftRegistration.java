package ch.epfl.javions.aircraft;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @param string aircraft registration
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record AircraftRegistration(String string) {
    static Pattern allowedStrings = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * @throws IllegalArgumentException if the given code is either empty or not a number, letter, or .?/_+-
     */
    public AircraftRegistration {
        if (!allowedStrings.matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
    }
}