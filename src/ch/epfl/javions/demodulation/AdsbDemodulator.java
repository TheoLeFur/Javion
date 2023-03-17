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
     * @return new message
     * @throws IOException if input/output error is encountered
     * @author : Theo Le Fur
     * Returns the next message in the stream
     */

    public RawMessage nextMessage() throws IOException {

        int pSum = 0, vSum, pSumPrevious = 0, pSumPosterior;

        while (this.powerWindow.isFull()) {


            pSum = this.pSum();
            vSum = this.vSum();
            pSumPosterior = this.PosteriorPSum();

            if ((pSum >= 2 * vSum && pSumPrevious < pSum && pSumPosterior < pSum)) {

                byte[] demodulatedMessage = new byte[MESSAGE_LENGTH / 8];

                for (int i : IntStream.range(0, demodulatedMessage.length).toArray()) {
                    demodulatedMessage[i] = this.bytes(8 * i);
                }

                long timeStampsNs = 100 * this.powerWindow.position();
                RawMessage message = RawMessage.of(timeStampsNs, demodulatedMessage);

                if (message != null && message.downLinkFormat() == 17) {
                    this.powerWindow.advanceBy(WINDOW_SIZE);
                    return message;
                }
            }
            pSumPrevious = pSum;
            this.powerWindow.advance();

        }
        return null;
    }

    private byte bytes(int index) {
        int b = 0;
        for (int i = 0; i < Byte.SIZE; i++) {
            b = (byte) (b | b(index + i) << (7 - i));
        }
        return (byte) b;
    }

    private int b(int index) {
        if (this.powerWindow.get(80 + 10 * index) < this.powerWindow.get(85 + 10 * index)) {
            return 0;
        } else return 1;
    }

    private int PosteriorPSum() {
        int[] indices = new int[]{0, 10, 35, 45};
        int pSum = 0;
        for (int i : indices) {
            pSum += this.powerWindow.get(i + 1);
        }
        return pSum;
    }

    private int pSum() {
        int[] indices = new int[]{0, 10, 35, 45};
        int pSum = 0;
        for (int i : indices) {
            pSum += this.powerWindow.get(i);
        }
        return pSum;
    }

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
            int n = 0;
            while ((m = d.nextMessage()) != null) {
                System.out.println(m);
                n++;
            }
            System.out.println(n);

        }
    }
}
