package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Class for navigating through the outputs of the power computer using a constant size window.
 * Defines some utility methods useful for accessing the relevant values.
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class PowerWindow {
    // Maximal size of window
    private final static int staticConstant = 65536;
    private final int windowSize;
    private long position = 0;
    private int[] tab1 = new int[staticConstant];
    private int[] tab2 = new int[staticConstant];
    private int batchSize;
    private final PowerComputer calculator;


    /**
     * Instantiates a power window, allowing us to navigate through the outputs of the Power Computer. The window's
     * maximal size is static constant.
     *
     * @param stream     stream containing signal data
     * @param windowSize size of window we want to instantiate, has to be smaller than staticConstant.
     * @throws IOException whenever exception thrown while reading the stream.
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument((windowSize > 0) && (windowSize <= staticConstant));
        this.windowSize = windowSize;
        this.calculator = new PowerComputer(stream, staticConstant);
        this.batchSize = this.calculator.readBatch(tab1);

    }

    /**
     * Get size of window
     *
     * @return size of window
     */
    public int size() {
        return windowSize;
    }

    /**
     * Gives current position of window.
     *
     * @return position
     */

    public long position() {
        return position;
    }

    /**
     * Indicates whether the power window is full or not
     *
     * @return true if the window is full, else false.
     */

    public boolean isFull() {
        return !(this.batchSize < staticConstant && windowSize + position % staticConstant > this.batchSize);
    }


    /**
     * @param i index
     * @return element at index i
     * Returns the element at index i in the window
     */

    public int get(int i) {
        Objects.checkIndex(i, this.windowSize);
        if (i + position % staticConstant < staticConstant) {
            return tab1[ (i + (int)position % staticConstant)];
        } else {
            return tab2[ (i - (staticConstant - (int) position % staticConstant))];
        }
    }

    /**
     * @throws IOException if error occurs while reading the batch
     *                     Advances the window by one increment
     */
    public void advance() throws IOException {
        position += 1;
        if (position % staticConstant == 0) {
            int[] tempTable = tab1;
            tab1 = tab2;
            tab2 = tempTable;
        }
        if ((position + windowSize - 1) % staticConstant == 0) {
            this.batchSize = calculator.readBatch(tab2);
        }

    }

    /**
     * @param offset offset
     * @throws IOException if error occurs while calling readBatch()
     *                     Advances the window by an offset
     */

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
