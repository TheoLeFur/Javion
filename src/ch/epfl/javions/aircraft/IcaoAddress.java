package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 * @param string decimal representation of the ICAO of the plane
 */
public record IcaoAddress(String string) {
    public final static int ICAO_ADDRESS_SIZE = 6;
    private final static Pattern allowedStrings = Pattern.compile("[0-9A-F]{6}");

    /**
     * @throws IllegalArgumentException if the given ICAO is not in hexadecimal representation or of length 6
     */
    public IcaoAddress {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches());
    }
}
