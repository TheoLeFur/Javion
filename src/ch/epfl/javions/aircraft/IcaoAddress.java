package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @param string decimal representation of the ICAO of the plane
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record IcaoAddress(String string) {
    public final static int ICAO_ADDRESS_SIZE = 6;
    private final static Pattern allowedStrings = Pattern.compile("[0-9A-F]{6}");

    /**
     * @param string Name of the address stored in a string
     */
    public IcaoAddress {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches());
    }
}
