package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class AdsbDemodulator {

    // Number of bits in message
    private static final int MESSAGE_LENGTH = 112;
    private static final int RAW_MESSAGE_LENGTH = MESSAGE_LENGTH / 8;
    private static final int TIME_STAMP_FACTOR = 100;
    // Byte buffer where the demodulated message will be stored.
    private static final byte[] demodulatedMessage = new byte[RAW_MESSAGE_LENGTH];
    // Size of power window
    private final int WINDOW_SIZE = 1200;
    private final PowerWindow powerWindow;

    /**
     * Instantiates a demodulator object.
     *
     * @param samplesStream stream of samples passed into the power window, with window size precised by the constant WINDOW_SIZE
     * @throws IOException when exception is thrown while reading the input stream.
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.powerWindow = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * @return new message
     * @throws IOException if input/output error is encountered
     *                     Returns the next message in the stream
     */

    public RawMessage nextMessage() throws IOException {

        int pSum;
        int vSum;
        int pSumPrevious = 0;
        int pSumPosterior = this.PosteriorPSum();

        while (this.powerWindow.isFull()) {
            pSum = pSumPosterior;
            vSum = this.vSum();
            pSumPosterior = this.PosteriorPSum();
            if (pSumPrevious < pSum && pSumPosterior < pSum && pSum >= 2 * vSum) {
                for (int i = 0; i < demodulatedMessage.length; i++) {
                    demodulatedMessage[i] = this.bytes(Byte.SIZE * i);
                }
                long timeStampsNs = TIME_STAMP_FACTOR * this.powerWindow.position();
                if (RawMessage.size(demodulatedMessage[0]) == RAW_MESSAGE_LENGTH) {
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
     * Builds bytes from a stream of bits.
     */

    private byte bytes(int index) {
        int b = 0;
        for (int i = 0; i < Byte.SIZE; i++) {
            b = (byte) (b | b(index + i) << (Byte.SIZE - 1 - i));
        }
        return (byte) b;
    }

    /**
     * @param index index of the signal in Power Window we are trying to demodulate
     * @return Demodulated bit signal
     * Computes the bits according to the demodulation convention
     */

    private int b(int index) {
        if (this.powerWindow.get(80 + 10 * index) < this.powerWindow.get(85 + 10 * index)) {
            return 0;
        } else return 1;
    }

    /**
     * @return posterior sum of peeks
     * Auxiliary function for determining sums of peaks
     */
    private int PosteriorPSum() {
        return this.powerWindow.get(1) + this.powerWindow.get(11) + this.powerWindow.get(36) + this.powerWindow.get(46);
    }


    /**
     * @return sum of valleys signals
     * Auxiliary function for determining sums of "valleys"
     */
    private int vSum() {
        return this.powerWindow.get(5) + this.powerWindow.get(15) + this.powerWindow.get(20) + this.powerWindow.get(25) + this.powerWindow.get(30) + this.powerWindow.get(40);
    }
}



