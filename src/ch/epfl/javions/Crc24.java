package ch.epfl.javions;

import java.math.BigInteger;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class Crc24 {

    //constant that represents the 24 weak bits of the generator used to calculate the crc24 of the ADSB messages
    public final static int GENERATOR = 0xFFF409;
    private final int[] builtTable;
    static int MAIN_TABLE_SIZE = 256;
    static int BITS_TO_EXTRACT = 24;
    /**
     * @param generator key that will be used for the cyclic redundancy check
     */
    public Crc24(int generator) {
        builtTable = buildTable(generator);
    }

    /**
     * method that produces all possible crc24 so as to not have to build them more than once to save algorithm costs
     *
     * @param generator key that is static and corresponds to the CRC variant used
     * @return a table that will contain all possible crc24
     */
    private static int[] buildTable(int generator) {
        int[] mainTable = new int[MAIN_TABLE_SIZE];
        byte[] tableByte;

        for (int i = 0; i < MAIN_TABLE_SIZE; i++) {
            tableByte = new byte[]{(byte) i};
            mainTable[i] = crc_bitwise(generator, tableByte);
        }

        return mainTable;
    }

    /**
     * this method executes the cyclic redundancy check with any generator on any table containing bytes bit by bit
     *
     * @param generator key that is static and corresponds to the CRC variant used
     * @param table     of bytes
     * @return crc24
     */
    private static int crc_bitwise(int generator, byte[] table) {
        int crc = 0;
        int b;
        int[] tab = {0, generator};

        //for loop that goes through each element of the message
        for (byte value : table) {
            for (int j = 0; j < Byte.SIZE; j++) {
                b = Bits.extractUInt(value, Byte.SIZE - 1 - j, 1);
                crc = ((crc << 1) | b) ^ tab[Bits.extractUInt(crc, BITS_TO_EXTRACT - 1, 1)];
            }
        }

        //for loop that goes through augmented part of the message
        for (int k = 0; k < BITS_TO_EXTRACT; k++) {
            crc = (crc << 1) ^ tab[Bits.extractUInt(crc, BITS_TO_EXTRACT - 1, 1)];
        }

        crc = Bits.extractUInt(crc, 0, BITS_TO_EXTRACT);

        return crc;
    }

    /**
     * this method executes the cyclic redundancy check with any generator on any table containing bytes, this time byte
     * by byte
     *
     * @param bytes array of bytes that when put together form the message
     * @return the crc24 of the given array of bytes
     */
    public int crc(byte[] bytes) {
        int crc = 0;

        //will represent a byte of the augmented message
        int o;

        for (byte aByte : bytes) {
            //now that we're working byte by byte there is a chance the corresponding integer will be negative since
            // they are signed so if the first bit of a byte is negative we add 256 to it
            o = Bits.extractUInt((aByte >= 0) ? aByte : aByte + MAIN_TABLE_SIZE, 0, Byte.SIZE);
            crc = ((crc << Byte.SIZE) | o) ^ builtTable[Bits.extractUInt(crc, BITS_TO_EXTRACT - Byte.SIZE, Byte.SIZE)];
        }

        for (int k = 0; k < BITS_TO_EXTRACT / Byte.SIZE; k++) {
            crc = (crc << Byte.SIZE) ^ builtTable[Bits.extractUInt(crc, BITS_TO_EXTRACT - Byte.SIZE, Byte.SIZE)];
        }

        crc = Bits.extractUInt(crc, 0, BITS_TO_EXTRACT);
        return crc;
    }

}