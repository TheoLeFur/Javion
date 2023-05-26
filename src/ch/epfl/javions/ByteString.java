package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class ByteString {

    private final byte[] bytes;
    private static final HexFormat hf = HexFormat.of().withUpperCase();

    /**
     * Initialises a ByteString object taking a array of bytes as an input
     *
     * @param bytes array of bytes
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /**
     * Parses a string of hexadecimal numbers to a ByteString
     *
     * @param hexString String to be parsed
     * @return Parsed ByteString
     */
    public static ByteString ofHexadecimalString(String hexString) {
        return new ByteString(hf.parseHex(hexString));
    }

    /**
     * Getter for a size of the Byte String
     *
     * @return Size of the String
     */

    public int size() {
        return this.bytes.length;
    }

    /**
     * Accesses the byte at a given index
     *
     * @param index Index from which we want to take the byte
     * @return Byte at index
     */
    public int byteAt(int index) {
        return Byte.toUnsignedInt(this.bytes[index]);
    }

    /**
     * Extracts long value in a specified range
     *
     * @param fromIndex Starting index
     * @param toIndex   End index
     * @return Long containing the bytes from fromIndex to toIndex.
     */

    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
        Preconditions.checkArgument(toIndex - fromIndex <= bytes.length);
        long value = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            value = (value << Byte.SIZE) + this.byteAt(i);
        }
        return value;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteString that) {
            if (this.size() != that.size()) {
                return false;
            } else return Arrays.equals(this.bytes, that.bytes);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return hf.formatHex(this.bytes);
    }


}
