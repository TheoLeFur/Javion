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
    //git@github.com:TheoLeFur/Javion.git

    public static final String message_dir = "/home/rudolf/IdeaProjects/eqihiohqoifqe/Javion/test/ch/epfl/test/messages_20230318_0915.bin";

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
                RawMessage m = new RawMessage(timeStampNs,message);
                //System.out.println(1);
                stateManager.updateWithMessage(m);
                //System.out.println(2);
                //System.out.println(m);
                for(ObservableAircraftState o: stateManager.states()) {
                    //System.out.println(3);
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