package ch.epfl.javions.demodulation;
import org.junit.jupiter.api.Test;

import javax.management.InstanceNotFoundException;
import java.io.*;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;


public class SamplesDecoderTest {


    private static final String path = "/Users/theolefur/Downloads/Javions/test/ch/epfl/test_ressources/samples.bin";
    private static final InputStream stream;

    static {
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DecodingIsDoneCorrectly() throws IOException {
        SamplesDecoder decoder = new SamplesDecoder(stream, 10);
        short[] buffer = new short[10];
        decoder.readBatch(buffer);
        short[] correctVals = new short[]{-3, 8, -9, -8, -5, -8, -12, -16, -23, -9};
        assertArrayEquals(buffer, correctVals);
    }

    @Test
    void ThrowsIllegalArgumentWhenBufferOfWrongSizeIsPassedInReadBatch() throws IOException{
        int batchSize = 1;
        int bufferSize = 2;
        SamplesDecoder decoder = new SamplesDecoder(stream, batchSize);
        short[] buffer = new short[bufferSize];
        assertThrows(IllegalArgumentException.class, () ->decoder.readBatch(buffer));
    }

    @Test
    void ConstructorRequiresBatchSizeStrictlyPositive(){
        int batchSize = 0;
        assertThrows(IllegalArgumentException.class, () -> new SamplesDecoder(stream, batchSize));
    }

    @Test
    void ConstructorThrowsNullPointerIfInputStreamIsNull(){
        int batchSize = 1;
        assertThrows(NullPointerException.class, () -> new SamplesDecoder(null, batchSize));
    }


    @Test
    void readBatchReturnsCorrectValue(){


    }

}
