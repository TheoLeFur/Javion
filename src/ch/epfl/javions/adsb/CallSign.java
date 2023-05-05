package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @param string call sign
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record CallSign(String string) {
    public final static int CALLSIGN_MAX_LENGTH = 8;
    static Pattern allowedStrings = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * @param string call sign
     * @throws IllegalArgumentException if the given string is not either the empty string, or a string of length 1 to 8
     *                                  consisting only of letters and numbers
     */
    public CallSign {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches() || string.isEmpty());
    }
}
