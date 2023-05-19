package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class for computing the power of signals by processing the stream batch-wise and computing the power
 * associated to it.
 *
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class PowerComputer {

    private final short[] buffer;
    private final List<Short> powerMemoryDeque;
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
        this.powerMemoryDeque = new ArrayList<>(Byte.SIZE);
        this.powerMemoryDeque.addAll(Collections.nCopies(Byte.SIZE, (short) 0));
    }

    /**
     * Computes the powers of the decoded messages, stores them in the inputted batch.
     *
     * @param batch batch where we store computed powers
     * @return number of powers thrown in batch
     * @throws IOException if Input/Output Exception occurs.
     */

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument((batch.length == this.buffer.length / 2));
        int count = this.decoder.readBatch(this.buffer);
        for (int i = 0; i < batchSize; i += 1) {
            this.addToList(this.buffer[2 * i]);
            this.addToList(this.buffer[2 * i + 1]);
            batch[i] = this.computeSignalPower();
        }
        return count / 2;
    }

    /**
     * Method for computing the signal's power
     *
     * @return corresponding power of the signal.
     */

    private int computeSignalPower() {
        return (int) (
                Math.pow(this.powerMemoryDeque.get(7) - this.powerMemoryDeque.get(5) + this.powerMemoryDeque.get(3) - this.powerMemoryDeque.get(1), 2)
                        + Math.pow(this.powerMemoryDeque.get(6) - this.powerMemoryDeque.get(4) + this.powerMemoryDeque.get(2) - this.powerMemoryDeque.get(0), 2)
        );
    }


    /**
     * Adds element to the top of the list. If the size of the list exceeds Byte.SIZE, then the last element is popped
     *
     * @param s element to be added
     */
    private void addToList(short s) {
        if (this.powerMemoryDeque.size() == Byte.SIZE)
            this.powerMemoryDeque.remove(0);
        this.powerMemoryDeque.add(s);
    }
}
