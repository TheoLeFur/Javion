package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

public class PowerWindow {
    InputStream stream;
    int windowSize;
    int position = 0;

    // Maximal size of window
    final static int staticConstant = 65536;
    int[] tab1 = new int[staticConstant];
    int[] tab2 = new int[staticConstant];
    int batchSize;
    PowerComputer calculator;

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
        this.stream = stream;
        this.windowSize = windowSize;
        this.calculator = new PowerComputer(stream, staticConstant);
        this.batchSize = this.calculator.readBatch(tab1);

    }

    /**
     * Get size of window
     *
     * @return size of window
     * @author Theo Le Fur SCIPER : 363294
     */
    public int size() {
        return windowSize;
    }

    /**
     * Gives current position of window.
     *
     * @return position
     * @author Theo Le Fur SCIPER : 363294
     */

    public long position() {
        return position;
    }

    /**
     * Indicates whether the power window is full or not
     *
     * @return true if the window is full, else false.
     * @author Theo Le Fur SCIPER : 363294
     */

    public boolean isFull() {
        return !(this.batchSize < staticConstant && windowSize + position % staticConstant > this.batchSize);
    }


    /**
     * @param i index
     * @return element at index i
     * @author Theo Le Fur SCIPER : 363294
     * Returns the element at index i in the window
     */

    public int get(int i) {
        if (!((i >= 0) && (windowSize > i))) {
            throw new IndexOutOfBoundsException();
        }
        if (i + position % staticConstant < staticConstant) {
            return tab1[i + position % staticConstant];
        } else {
            return tab2[(i - (staticConstant - position % staticConstant))];
        }
    }

    /**
     * @throws IOException if error occurs while reading the batch
     * @author Theo Le Fur SCIPER : 363294
     * Advances the window by one increment
     */
    public void advance() throws IOException {
        position += 1;
        int[] tempTable;
        if (position % staticConstant == 0) {
            tempTable = tab1.clone();
            tab1 = tab2.clone();
            tab2 = tempTable.clone();
        }
        if ((position + windowSize - 1) % staticConstant == 0) {
            this.batchSize = calculator.readBatch(tab2);
        }

    }

    /**
     * @param offset offset
     * @throws IOException if error occurs while calling readBatch()
     * @author Theo Le Fur SCIPER : 363294
     * Advances the window by an offset
     */

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
