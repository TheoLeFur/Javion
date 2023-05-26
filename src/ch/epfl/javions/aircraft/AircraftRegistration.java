package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @param string aircraft registration
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record AircraftRegistration(String string) {
    private final static Pattern allowedStrings = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * @throws IllegalArgumentException if the given code is either empty or not a number, letter, or .?/_+-
     */
    public AircraftRegistration {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches() || string.isEmpty());
    }
}