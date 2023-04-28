package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @param string decimal representation of the ICAO of the plane
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record IcaoAddress(String string) {


    private static final Pattern allowedStrings = Pattern.compile("[0-9A-F]{6}");

    /**
     * @param string Name of the address stored in a string
     */
    public IcaoAddress {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches());
    }
}
