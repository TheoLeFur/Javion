package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * This class instantiates a mechanism for decoding raw messages coming from the aircraft.
 *
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class SamplesDecoder {

    private final InputStream stream;
    private final int batchSize;
    private final byte[] buffer;

    /**
     * Instantiates a samples decoder object. Builds the buffer where the bits of the signal will be stored.
     *
     * @param stream    stream containing signal to decode
     * @param batchSize size of batch
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        this.buffer = new byte[Short.BYTES * this.batchSize];
    }

    /**
     * Reads batch from stream and transforms it into a 12 bit representation
     *
     * @param batch batch where values from stream are stored
     * @return Number of elements that have been transformed
     * @throws IOException whenever error is raised while reading the stream.
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == this.batchSize);
        int N = stream.readNBytes(buffer, 0, this.buffer.length);
        for (int i = 0; i < batchSize; i++) {
            int bufferIndex = 2 * i;
            byte strongByte = this.buffer[bufferIndex];
            byte weakByte = this.buffer[bufferIndex + 1];
            short concat = (short) (strongByte << Byte.SIZE | weakByte);
            int THRESHOLD = 2048;
            batch[i] = (short) (Short.reverseBytes(concat) - THRESHOLD);
            // TODO : find some way to not use the reverseByte method (no real reason why, it is very efficient).

        }
        return N/2;
    }
}

