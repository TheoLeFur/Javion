package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public record AircraftDescription(String string) {
    private final static Pattern allowedStrings = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * @param string aircraft description
     * @throws IllegalArgumentException if the given code's first element is not A,B,D,G,H,L,P,R,S,T,V,- the second
     *                                  element 0,1,2,3,4,6,8 and the third element E,J,P,T,- (the empty string is a valid description)
     */
    public AircraftDescription {
        Preconditions.checkArgument(allowedStrings.matcher(string).matches() || string.isEmpty());
    }
}