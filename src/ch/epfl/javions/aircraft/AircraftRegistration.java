package ch.epfl.javions.aircraft;
import java.util.ArrayList;
import java.util.regex.Pattern;


public record AircraftRegistration(String string) {
    static Pattern allowedStrings = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * @author Rudolf Yazbeck
     * @param string aircraft registration
     * @throws IllegalArgumentException if the given code is either empty or not a number, letter, or .?/_+-
     */
    public AircraftRegistration{
        if(!allowedStrings.matcher(string).matches()) {throw new IllegalArgumentException();}
    }
}