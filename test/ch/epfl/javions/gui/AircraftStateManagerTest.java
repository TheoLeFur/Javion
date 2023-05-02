package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class AircraftStateManagerTest {

<<<<<<< HEAD
    public static final String message_dir = "/Users/theolefur/Javion/resources/messages_20230318_0915.bin";
=======
    public static final String message_dir = "/Users/theolefur/Downloads/Javions/resources/messages_20230318_0915.bin";
>>>>>>> parent of 6ca0958 (I have exchanged files that I wrote)

    public static void main(String[] args) {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(message_dir)))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        } catch (EOFException e) { /* nothing to do */ } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void aircraftStateManagerTest(){
        AircraftStateManager stateManager=new AircraftStateManager(new AircraftDatabase("/aircraft.zip"));
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(message_dir)))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
<<<<<<< HEAD
                RawMessage m = new RawMessage(timeStampNs,message);
=======
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                if(m==null)break;
                //System.out.println(1);
>>>>>>> parent of 6ca0958 (I have exchanged files that I wrote)
                stateManager.updateWithMessage(m);
                for(ObservableAircraftState o: stateManager.states()) {
                    if(o.getAircraftData() != null) {
                        System.out.println(o.getIcaoAddress().toString() +
                                o.getCallSign() + o.getRegistration() +
                                o.getModel() + "   position: " +
                                o.getPosition().longitude() +
                                o.getPosition().latitude() + o.getAltitude() + o.getVelocity());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void states() {
    }

    @Test
    void updateWithMessage() {
    }

    @Test
    void purge() {
    }
}