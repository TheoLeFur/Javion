package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class PowerComputer {

    private short[] buffer;

    private short[] powerMemory;
    private SamplesDecoder decoder;
    private int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize % 8 == 0 || batchSize <= 0);
        this.batchSize = batchSize;
        this.buffer = new short[2 * batchSize];
        this.decoder = new SamplesDecoder(stream, 2 * batchSize);
        this.powerMemory = new short[8];

    }

    /**
     * Computes the powers of the decoded messages, stores them in the inputted batch.
     *
     * @param batch batch where we store computed powers
     * @return number of powers thrown in batch
     * @throws IOException if Input/Output Exception occurs.
     * @author Theo Le Fur
     */

    public int readBatch(int[] batch) throws IOException {

        int numberOfPowers = 0;

        Preconditions.checkArgument(batch.length == this.buffer.length / 2);
        int count = this.decoder.readBatch(this.buffer);

        for (int i = 0; i < batchSize; i++) {
            for (int k = 0; k < this.powerMemory.length - 2; k++) {
                powerMemory[k] = powerMemory[k + 2];
            }
            powerMemory[7] = this.buffer[2 * i + 1];
            powerMemory[6] = this.buffer[2 * i];

            batch[i] = (int) (Math.pow(powerMemory[7] - powerMemory[5] + powerMemory[3] - powerMemory[1], 2) +
                    Math.pow(powerMemory[6] - powerMemory[4] + powerMemory[2] - powerMemory[0], 2));
            numberOfPowers++;

        }
        return count / 2;
    }
}
