package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcaoAddressTest {

    /**
     * @author Rudolf Yazbeck
     */
    @Test
    void string() {
        //checking if a clearly non-hexadecimal representation sends an error
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("0F090G"));

        //checking if the limit to the address size really is 6
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("0000000"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("00000"));

        //creating an instance that shouldn't return any errors just in case
        new IcaoAddress("076ABF");

        //checking wether the empty string works or not
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress(""));
    }
    @Test
    void icaoAddressConstructorThrowsWithInvalidAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("00000a");
        });
    }

    @Test
    void icaoAddressConstructorThrowsWithEmptyAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("");
        });
    }

    @Test
    void icaoAddressConstructorAcceptsValidAddress() {
        assertDoesNotThrow(() -> {
            new IcaoAddress("ABCDEF");
        });
    }
}