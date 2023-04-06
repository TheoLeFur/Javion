package ch.epfl.javions.aircraft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public record IcaoAddress(String string) {
    static Pattern allowedStrings = Pattern.compile("[0-9A-F]{6}");

    /**
     * @param string decimal representation of the ICAO of the plane
     * @throws IllegalArgumentException if the given ICAO is not in hexadecimal representation or of length 6
     * @author Rudolf Yazbeck (SCIPER 360700)
     */
    public IcaoAddress {
        if (!allowedStrings.matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
    }
}
