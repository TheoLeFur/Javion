package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftStateAccumulatorTest {

    class AircraftState implements AircraftStateSetter {
        @Override
        public void setLastMessageTimeStampNs(long timeStampNs) {

        }

        @Override
        public void setCategory(int category) {

        }

        @Override
        public void setCallSign(CallSign callSign) {
            System.out.println("Call Sign : " + callSign);
        }

        @Override
        public void setPosition(GeoPos position) {
            System.out.println("Position : " + position);
        }

        @Override
        public void setAltitude(double altitude) {

        }

        @Override
        public void setVelocity(double velocity) {

        }

        @Override
        public void setTrackOrHeading(double trackOrHeading) {

        }
    }

    @Test
    void ConstructorThrowsNullPointerExceptionCorrectly() {
        AircraftStateSetter stateSetter = null;
        assertThrows(NullPointerException.class, () -> new AircraftStateAccumulator<AircraftStateSetter>(stateSetter));
    }

    @Test
    void PrintsTheCorrectValues() throws IOException {
        String f = "/Users/theolefur/Downloads/Javions/resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a =
                    new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;
                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }
}


