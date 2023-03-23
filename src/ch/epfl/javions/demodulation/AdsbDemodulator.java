package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

public final class AdsbDemodulator {

    InputStream samplesStream;
    PowerWindow powerWindow;
    private final int MESSAGE_LENGTH = 112;
    private final int WINDOW_SIZE = 1200;

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.samplesStream = samplesStream;
        this.powerWindow = new PowerWindow(this.samplesStream, WINDOW_SIZE);
    }

    /**
     * @author Theo Le Fur
     * @return new message
     * @throws IOException if input/output error is encountered
     * @author : Theo Le Fur
     * Returns the next message in the stream
     */

    public RawMessage nextMessage() throws IOException {

        int pSum = this.pSum();
        int vSum = this.vSum();
        int pSumPrevious = 0;
        int pSumPosterior = this.PosteriorPSum();

        while (this.powerWindow.isFull()) {
            pSum = pSumPosterior;
            vSum = this.vSum();
            pSumPosterior = this.PosteriorPSum();
            if (pSumPrevious < pSum && pSumPosterior < pSum && pSum >= 2 * vSum ) {
                byte[] demodulatedMessage = new byte[MESSAGE_LENGTH / 8];
                for (int i = 0; i < demodulatedMessage.length; i++) {
                    demodulatedMessage[i] = this.bytes(8 * i);
                }
                long timeStampsNs = 100 * this.powerWindow.position();
                RawMessage message = RawMessage.of(timeStampsNs, demodulatedMessage);
                if (message != null) {
                    if (message.downLinkFormat() == 17) {
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
     * @author Theo Le Fur
     * Builds bytes from a stream of bits
     * @param index index of element in the power window
     * @return byte
     */

    private byte bytes(int index) {
        int b = 0;
        for (int i = 0; i < Byte.SIZE; i++) {
            b = (byte) (b | b(index + i) << (7 - i));
        }
        return (byte) b;
    }

    /**

     * @author Theo Le Fur
     * Computes the bits according to the demodulation convention
     * @param index index of the signal in Power Window we are trying to demodulate
     * @return Demodulated bit signal
     */

    private int b(int index) {
        if (this.powerWindow.get(80 + 10 * index) < this.powerWindow.get(85 + 10 * index)) {
            return 0;
        } else return 1;
    }

    /**
     * Auxiliary function for determining sums of peaks
     * @return posterior sum of peeks
     */
    private int PosteriorPSum() {
        int[] indices = new int[]{0, 10, 35, 45};
        int pSum = 0;
        for (int i : indices) {
            pSum += this.powerWindow.get(i + 1);
        }
        return pSum;
    }


    /**
     * @author Theo Le Fur

     * Auxiliary function for determining sums of peaks
     * @return sum of peeks
     */
    private int pSum() {
        int[] indices = new int[]{0, 10, 35, 45};
        int pSum = 0;
        for (int i : indices) {
            pSum += this.powerWindow.get(i);
        }
        return pSum;
    }

    /**
     * @author Theo Le Fur
     * Auxiliary function for determining sums of "valleys"
     * @return sum of valleys signals
     */
    private int vSum() {
        int[] indices = new int[]{5, 15, 20, 25, 30, 40};
        int vSum = 0;
        for (int i : indices) {
            vSum += this.powerWindow.get(i);
        }
        return vSum;
    }
}

class PrintRawMessages {
    public static void main(String[] args) throws IOException {
        String f = "/Users/theolefur/Downloads/Javions/resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                System.out.println(m);
            }
        }
    }
}
