package ch.epfl.javions;

public final class Bits {

    private Bits(){}
    /**
     * @return an extracted vector from a 64 bit vector
     * @param start starting index
     * @param size length of the bit
     * @param value 64 bit vector from which the bit is extracted
     * @throws IllegalArgumentException if the length of the bit to be extracted is >= 32
     * @throws IndexOutOfBoundsException if the interval described by the start and size is not entirely comprised between 0 included and 64 excluded
     * */

    public static int extractUInt(long value, int start, int size) {

        if (size <= 0 || size >= Integer.SIZE) throw new IllegalArgumentException();
        else if (start + size > Long.SIZE || start < 0) throw new IndexOutOfBoundsException();
        else {
            long shiftedValue = value >>> start;
            long mask = (1 << size) - 1;
            return (int) (shiftedValue & mask);
        }
    }

    /**
     * @return true if and only if the indexed bit is a 1 going from right to left
     * @throws IndexOutOfBoundsException if the described interval is not between 0 included and 64 excluded*/
    public static boolean testBit(long value, int index){
        if (index < 0 || index >= 64) throw new IndexOutOfBoundsException();
        else {
            long mask = 1L << index;
            return ((mask & value) == value);
        }
    }
}
