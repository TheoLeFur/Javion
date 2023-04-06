package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;


public record AircraftDescription(String string) {
    static Pattern allowedStrings = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * @param string aircraft description
     * @throws IllegalArgumentException if the given code's first element is not A,B,D,G,H,L,P,R,S,T,V,- the second
     *                                  element 0,1,2,3,4,6,8 and the third element E,J,P,T,- (the empty string is a valid description)
     * @author Rudolf Yazbeck (SCIPER 360700)
     */
    public AircraftDescription {
        if (!allowedStrings.matcher(string).matches() && !string.equals("")) {
            throw new IllegalArgumentException();
        }
    }
}