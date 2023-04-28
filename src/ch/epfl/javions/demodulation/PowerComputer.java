package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for computing the power of signals by processing the stream batch-wse and computing the power
 * associated to it.
 *
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class PowerComputer {

    private final short[] buffer;

    private final short[] powerMemory;
    private final SamplesDecoder decoder;
    private final int batchSize;

    /**
     * Instantiates a Power Computer objet. Processes input stream batch-wise.Then computes the powers associated to the input signal
     *
     * @param stream    stream we will be processing batch by batch
     * @param batchSize sie of batch
     * @throws IllegalArgumentException whenever the batchSize is not divisible by 8, or whenever non-positive
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize % Byte.SIZE == 0 && batchSize > 0);
        this.batchSize = batchSize;
        this.buffer = new short[Short.BYTES * batchSize];
        this.decoder = new SamplesDecoder(stream, this.buffer.length);
        this.powerMemory = new short[Byte.SIZE];

    }

    /**
     * Computes the powers of the decoded messages, stores them in the inputted batch.
     *
     * @param batch batch where we store computed powers
     * @return number of powers thrown in batch
     * @throws IOException if Input/Output Exception occurs.
     */

    public int readBatch(int[] batch) throws IOException {


        Preconditions.checkArgument(batch.length == this.buffer.length / 2);
        int count = this.decoder.readBatch(this.buffer);
        for (int i = 0; i < batchSize; i++) {
            for (int k = 0; k < this.powerMemory.length - 2; k++) {
                powerMemory[k] = powerMemory[k + 2];
            }
            // TODO : find a more efficient way to realise this permutation
            powerMemory[7] = this.buffer[2 * i + 1];
            powerMemory[6] = this.buffer[2 * i];

            batch[i] = (int) (Math.pow(powerMemory[7] - powerMemory[5] + powerMemory[3] - powerMemory[1], 2) +
                    Math.pow(powerMemory[6] - powerMemory[4] + powerMemory[2] - powerMemory[0], 2));
        }
        return count / 2;
    }
}
