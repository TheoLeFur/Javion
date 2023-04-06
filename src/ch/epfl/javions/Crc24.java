package ch.epfl.javions;

import java.math.BigInteger;

public final class Crc24 {


    //constant that represents the 24 weak bits of the generator used to calculate the crc24 of the ADSB messages
    public final static int GENERATOR = 0xFFF409;
    static int[] builtTable;

    /**
     * @param generator key that will be used for the cyclic redundancy check
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    public Crc24(int generator) {
        builtTable = buildTable(generator);
    }

    /**
     * @param generator key that is static and corresponds to the CRC variant used
     * @return a table that will contain all possible crc24 so as to do them all at once to save algorithm costs
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    private static int[] buildTable(int generator) {
        int[] mainTable = new int[256];
        byte[] tableByte;

        for (int i = 0; i < 256; i++) {
            tableByte = new byte[]{(byte) i};
            mainTable[i] = crc_bitwise(generator, tableByte);
        }

        return mainTable;
    }

    /**
     * @param bytes array of bytes that when put together form the message
     * @return the crc24 of the given array of bytes
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    public int crc(byte[] bytes) {
        int crc = 0;

        //will represent a byte of the augmented message
        int o;

        //constant that represents number of crc bits to extract the end of the algorithm
        int N = 24;

        for (byte aByte : bytes) {
            //now that we're working byte by byte there is a chance the corresponding integer will be negative since
            // they are signed so if the first bit of a byte is negative we add 256 to it
            o = Bits.extractUInt((aByte >= 0) ? aByte : aByte + 256, 0, 8);
            crc = ((crc << 8) | o) ^ builtTable[Bits.extractUInt(crc, N - 8, 8)];
        }

        for (int k = 0; k < 3; k++) {
            crc = (crc << 8) ^ builtTable[Bits.extractUInt(crc, N - 8, 8)];
        }

        crc = Bits.extractUInt(crc, 0, N);
        return crc;
    }

    /**
     * @param generator key that is static and corresponds to the CRC variant used
     * @param table     of bytes
     * @return crc24
     * @author Rudolf Yazbeck (SCIPER: 360700)
     */
    private static int crc_bitwise(int generator, byte[] table) {
        int crc = 0;
        int b;
        int N = 24;
        int[] tab = {0, generator};

        //for loop that goes through each element of the message
        for (byte value : table) {
            for (int j = 0; j < 8; j++) {
                b = Bits.extractUInt(value, 7 - j, 1);
                crc = ((crc << 1) | b) ^ tab[Bits.extractUInt(crc, N - 1, 1)];
            }
        }

        //for loop that goes through augmented part of the message
        for (int k = 0; k < N; k++) {
            crc = (crc << 1) ^ tab[Bits.extractUInt(crc, N - 1, 1)];
        }

        crc = Bits.extractUInt(crc, 0, N);

        return crc;
    }

}