package ch.epfl.javions.adsb;

import java.util.regex.Pattern;

public record CallSign(String string) {
    static Pattern allowedStrings = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * @param string representing the callsign
     * @throws IllegalArgumentException if the given string is not either the empty string, or a string of length 1 to 8
     *                                  consisting only of letters and numbers
     * @author Rudolf Yazbeck (SCIPER 360700)
     */
    public CallSign {
        if (!allowedStrings.matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
    }
}
