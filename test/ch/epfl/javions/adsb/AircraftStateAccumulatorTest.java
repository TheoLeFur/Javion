package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AircraftState implements AircraftStateSetter {
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        //System.out.println("Time Stamps : " + timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        //System.out.println("Category : " + category);
    }

    @Override
    public void setCallSign(CallSign callSign) {

        System.out.println("indicatif : " + callSign);
    }

    @Override
    public void setAltitude(double altitude) {
        //System.out.println("Altitude : " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        //System.out.println("Vitesse : " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        //System.out.println("Direction : " + trackOrHeading);
    }

    @Override
    public void setPosition(GeoPos position) {

        System.out.println("position : " + position);
    }
}
    /*Values:
    position : (5.620176717638969°, 45.71530147455633°)
    position : (5.621292097494006°, 45.715926848351955°)
    indicatif : CallSign[string=RYR7JD]
    position : (5.62225341796875°, 45.71644593961537°)
    position : (5.623420681804419°, 45.71704415604472°)
    position : (5.624397089704871°, 45.71759032085538°)
    position : (5.625617997720838°, 45.71820789948106°)
    position : (5.626741759479046°, 45.718826316297054°)
    position : (5.627952609211206°, 45.71946484968066°)
    position : (5.629119873046875°, 45.72007002308965°)
    position : (5.630081193521619°, 45.7205820735544°)
    position : (5.631163045763969°, 45.72120669297874°)
    indicatif : CallSign[string=RYR7JD]
    position : (5.633909627795219°, 45.722671514377°)
    position : (5.634819064289331°, 45.72314249351621°)
    */

class AircraftStateAccumulatorTest {
    public static void main(String[] args) throws IOException {
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


