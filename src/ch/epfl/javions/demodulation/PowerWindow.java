package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

public class PowerWindow {
    InputStream stream;
    int windowSize;
    int position = 0;
    final static int staticConstant = 65536;
    int[] tab1 = new int[staticConstant];
    int[] tab2 = new int[staticConstant];
    int batchSize;
    PowerComputer calculator;


    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument((windowSize > 0) && (windowSize <= staticConstant));
        this.stream = stream;
        this.windowSize = windowSize;
        this.calculator = new PowerComputer(stream, staticConstant);
        this.batchSize = this.calculator.readBatch(tab1);

    }

    /**
     * Get size of window
     * @return size of window
     */
    public int size() {
        return windowSize;
    }

    /**
     * Gives position of window.
     * @return position
     */

    public long position() {
        return position;
    }

    /**
     * Indicates whether the window is full or not
     * @return true if the window is full, else false.
     */

    public boolean isFull(){
        return !(this.batchSize < staticConstant && windowSize + position%staticConstant > this.batchSize);
    }


    /**
     * Returns the element at index i in the window
     * @param i index
     * @return element at index i
     */

    public int get(int i) {
        if (!((i >= 0) && (windowSize > i))) {
            throw new IndexOutOfBoundsException();
        }
        if (i + position% staticConstant < staticConstant) {
            return tab1[i + position % staticConstant];
        } else {
            return tab2[(i - (staticConstant - position % staticConstant))];
        }
    }

    /**
     * Advances the window by one increment
     * @throws IOException if error occurs whenever we read the batch
     */
    public void advance() throws IOException {
        position += 1;
        int[] tempTable;
        if (position % staticConstant == 0){
            tempTable = tab1.clone();
            tab1 = tab2.clone();
            tab2 = tempTable.clone();
        }
        if ((position + windowSize - 1) % staticConstant == 0){
            this.batchSize = calculator.readBatch(tab2);
        }

    }

    /**
     * Advances the window by an offset
     * @param offset offset
     * @throws IOException if error occurs while calling readBatch()
     */

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i : IntStream.range(0, offset).toArray()){
            advance();
        }
    }
}
