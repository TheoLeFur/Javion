package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public final class SamplesDecoder {


    private final InputStream stream;
    private final int batchSize;
    private byte[] buffer;


    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        this.buffer = new byte[2 * this.batchSize];
    }

    /**
     * Reads batch from stream and transforms it into a 12 bit representation
     *
     * @param batch batch where values from stream are stored
     * @return Number of elements that have been transformed
     * @throws IOException
     * @author Theo le Fur
     */
    public int readBatch(short[] batch) throws IOException {
        int N;
        Preconditions.checkArgument(batch.length == this.batchSize);
        this.buffer = stream.readNBytes(2 * batchSize);
        N = this.buffer.length / 2;
        for (int i = 0; i < N; i++) {
            int bufferIndex = 2 * i;
            byte strongByte = this.buffer[bufferIndex];
            byte weakByte = this.buffer[bufferIndex + 1];
            short concat = (short) (strongByte << 8 | weakByte);
            int THRESHOLD = 2048;
            batch[i] = (short) (Short.reverseBytes(concat) - THRESHOLD);
        }

        return N;
    }
}

