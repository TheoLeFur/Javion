package ch.epfl.javions;

import java.util.Objects;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class Bits {

    /**
     * Private constructor, no instantiation possible
     */
    private Bits() {
    }

    /**
     * Extracts a unsigned integer from a long value in a range specified in the params.
     *
     * @param start starting index
     * @param size  length of the bit
     * @param value 64 bit vector from which the bit is extracted
     * @return an extracted vector from a 64 bit vector
     * @throws IllegalArgumentException  if the length of the bit to be extracted is >= 32
     * @throws IndexOutOfBoundsException if the interval described by the start and size is not entirely comprised between 0 included and 64 excluded
     */

    public static int extractUInt(long value, int start, int size) {

        Preconditions.checkArgument(size >= 0 && size < Integer.SIZE);
        if (start + size > Long.SIZE || start < 0) throw new IndexOutOfBoundsException();
        else {
            long shiftedValue = value >>> start;
            long mask = (1 << size) - 1;
            return (int) (shiftedValue & mask);
        }
    }

    /**
     * Method for verifying if a bit at a specified index is 1 or 0.
     *
     * @return true if and only if the indexed bit is a 1 going from right to left
     * @throws IndexOutOfBoundsException if the described interval is not between 0 included and 64 excluded
     */
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        long mask = 1L << index;
        return ((mask & value) == value);

    }
}
