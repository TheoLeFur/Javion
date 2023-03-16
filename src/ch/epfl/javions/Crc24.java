package ch.epfl.javions;

import java.math.BigInteger;

public final class Crc24 {
    public final static int GENERATOR = 0xFFF409;
    static int[] builtTable;

    //not needed for the non final optimised version
    public Crc24(int generator) {
        builtTable = buildTable(generator);
    }

    /**
     * @author Rudolf Yazbeck
     * @param generator key that is static
     * @return a table that will contain all possible crc24
     */
    private static int[] buildTable(int generator) {
        int[] mainTable = new int[256];
        byte[] tableByte;

        for(int i = 0; i < 256; i++) {
            tableByte = new byte[]{(byte)i};
            mainTable[i] = crc_bitwise(generator, tableByte);
        }

        return mainTable;
    }

    /**
     * @param bytes array of bytes that when put together form the message
     * @return the corresponding crc24
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        int o;
        int N = 24;

        for(int i = 0; i < bytes.length; i++) {
            //now that we're working bit by bit there is a chance it will be negative since they are signed so
            //if the first bit of a byte is negative we add 256 to it
            o = Bits.extractUInt((bytes[i]>=0)? bytes[i] : bytes[i]+256, 0, 8);
            crc = ((crc << 8) | o) ^ builtTable[Bits.extractUInt(crc, N-8, 8)];
        }

        for (int k = 0; k<3; k++){
            crc = (crc << 8) ^ builtTable[Bits.extractUInt(crc, N-8, 8)];
        }

        crc = Bits.extractUInt(crc, 0, N);
        return crc;
    }

    /**
     * @author Rudolf Yazbeck
     * @param generator key that is static
     * @param table of bytes
     * @return crc24
     */
    private static int crc_bitwise(int generator, byte[] table) {
        int crc = 0;
        int b;
        int N = 24;
        int[] tab = {0, generator};

        //for loop that goes through each element of the message
        for(int i = 0; i < table.length ; i++ ) {
            for(int j = 0; j < 8; j++) {
                b = Bits.extractUInt(table[i], 7 - j, 1);
                crc = ((crc << 1) | b) ^ tab[Bits.extractUInt(crc, N-1, 1)];
            }
        }

        //for loop that goes through augmented part of the message
        for (int k = 0; k<N; k++){
            crc = (crc << 1) ^ tab[Bits.extractUInt(crc, N-1, 1)];
        }

        crc = Bits.extractUInt(crc, 0, N);

        return crc;
    }

}