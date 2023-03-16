package ch.epfl.javions.aircraft;
import java.util.regex.Pattern;


public record AircraftTypeDesignator(String string) {
    static Pattern allowedStrings = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * @author Rudolf Yazbeck
     * @throws IllegalArgumentException if the given pattern is not a number or a letter and of length 2 to 4 (the
     * empty string is a valid designator)
     * @param string aircraft's pattern
     */
    public AircraftTypeDesignator{
        if(!allowedStrings.matcher(string).matches() && !string.equals("")) {throw new IllegalArgumentException();}
    }
}