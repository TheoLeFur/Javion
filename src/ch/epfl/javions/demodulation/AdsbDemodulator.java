package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

public final class AdsbDemodulator {

    InputStream samplesStream;
    PowerWindow powerWindow;
    private static final int MESSAGE_LENGTH = 112;
    private final int WINDOW_SIZE = 1200;
    private static final byte[] demodulatedMessage = new byte[MESSAGE_LENGTH / 8];

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.samplesStream = samplesStream;
        this.powerWindow = new PowerWindow(this.samplesStream, WINDOW_SIZE);
    }

    /**
     * @return new message
     * @throws IOException if input/output error is encountered
     * @author Theo Le Fur SCIPER : 363294
     * Returns the next message in the stream
     */

    public RawMessage nextMessage() throws IOException {

        int pSum = 0;
        int vSum = 0;
        int pSumPrevious = 0;
        int pSumPosterior = this.PosteriorPSum();

        while (this.powerWindow.isFull()) {
            pSum = pSumPosterior;
            vSum = this.vSum();
            pSumPosterior = this.PosteriorPSum();
            if (pSumPrevious < pSum && pSumPosterior < pSum && pSum >= 2 * vSum) {
                for (int i = 0; i < demodulatedMessage.length; i++) {
                    demodulatedMessage[i] = this.bytes(8 * i);
                }
                long timeStampsNs = 100 * this.powerWindow.position();
                if (RawMessage.size(demodulatedMessage[0]) == 14) {
                    RawMessage message = RawMessage.of(timeStampsNs, demodulatedMessage);
                    if (message != null) {
                        this.powerWindow.advanceBy(WINDOW_SIZE);
                        return message;
                    }
                }
            }
            pSumPrevious = pSum;
            this.powerWindow.advance();
        }
        return null;
    }


    /**
     * @param index index of element in the power window
     * @return byte
     * @author Theo Le Fur SCIPER : 363294
     * Builds bytes from a stream of bits
     */

    private byte bytes(int index) {
        int b = 0;
        for (int i = 0; i < Byte.SIZE; i++) {
            b = (byte) (b | b(index + i) << (7 - i));
        }
        return (byte) b;
    }

    /**
     * @param index index of the signal in Power Window we are trying to demodulate
     * @return Demodulated bit signal
     * @author Theo Le Fur SCIPER : 363294
     * Computes the bits according to the demodulation convention
     */

    private int b(int index) {
        if (this.powerWindow.get(80 + 10 * index) < this.powerWindow.get(85 + 10 * index)) {
            return 0;
        } else return 1;
    }

    /**
     * @return posterior sum of peeks
     * @author Theo Le Fur SCIPER : 363294
     * Auxiliary function for determining sums of peaks
     */
    private int PosteriorPSum() {
        return this.powerWindow.get(1) + this.powerWindow.get(11) + this.powerWindow.get(36) + this.powerWindow.get(46);
    }


    /**
     * @return sum of peeks
     * @author Theo Le Fur SCIPER : 363294
     * Auxiliary function for determining sums of peaks
     */
    private int pSum() {
        return this.powerWindow.get(0) + this.powerWindow.get(10) + this.powerWindow.get(35) + this.powerWindow.get(45);
    }

    /**
     * @return sum of valleys signals
     * @author Theo Le Fur SCIPER : 363294
     * Auxiliary function for determining sums of "valleys"
     */
    private int vSum() {
        return this.powerWindow.get(5) + this.powerWindow.get(15) + this.powerWindow.get(20) + this.powerWindow.get(25) + this.powerWindow.get(30) + this.powerWindow.get(40);
    }
}



