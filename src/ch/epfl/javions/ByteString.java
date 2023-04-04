package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

public final class ByteString {

    private final byte[] bytes;

    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /**
     * Parses a string of hexadecimal numbers to a ByteString
     *
     * @param hexString String to be parsed
     * @return Parsed ByteString
     * @author Theo Le Fur
     */
    public static ByteString ofHexadecimalString(String hexString) {
        HexFormat hf = HexFormat.of().withUpperCase();
        return new ByteString(hf.parseHex(hexString));
    }

    /**
     * Size of the String
     *
     * @return SIze of the String
     * @author Theo Le Fur
     */

    public int size() {
        return this.bytes.length;
    }

    /**
     * Returns the byte at a specific index
     *
     * @param index Index from which we want to take the byte
     * @return Byte at index
     * @author Theo Le Fur
     */
    public int byteAt(int index) {
        if (index >= this.bytes.length) {
            throw new IndexOutOfBoundsException();
        } else return this.bytes[index] & 0xff;
    }

    /**
     * Long in a certain range
     *
     * @param fromIndex Starting index
     * @param toIndex   End index
     * @return Long containing the bytes from fromIndex to toIndex.
     * @author Theo Le Fur SCIPER : 363294
     */

    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
        if (toIndex - fromIndex > bytes.length) {
            throw new IllegalArgumentException();
        } else {
            long value = 0;
            for (int i = fromIndex; i < toIndex; i++) {
                value = (value << 8) + (this.bytes[i] & 255);
            }
            return value;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteString that) {
            if (this.size() != that.size()) {
                return false;
            } else {
                for (int i = 0; i < this.size(); i++) {
                    if (this.byteAt(i) != that.byteAt(i)) {
                        return false;
                    }
                }
            }
            return true;
        } else return false;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        HexFormat hf = HexFormat.of().withUpperCase();
        return hf.formatHex(this.bytes);
    }


}
