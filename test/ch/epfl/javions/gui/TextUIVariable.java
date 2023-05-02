package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Thread.sleep;

//java --enable-preview --source 17 -cp out/production/Javions/ --module-path C:\Users\User\Desktop\openjfx-20_windows-x64_bin-sdk\javafx-sdk-20\lib --add-modules javafx.controls Javions/src/ch/epfl/javions/gui/TextUI.java
public class TextUIVariable {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("/home/rudolf/IdeaProjects/eqihiohqoifqe/Javion/test/ch/epfl/test/messages_20230318_0915.bin")))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            RawMessage m;
            AircraftDatabase test = new AircraftDatabase("/aircraft.zip");
            AircraftStateManager asm = new AircraftStateManager(test);
            long time = 0;
            int i = 0;

            System.out.println();
            List<ObservableAircraftState> order = new ArrayList<>(asm.states());
            List<ObservableAircraftState> previousOrder = new ArrayList<>();
            while (true) {
                sleep(100);
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                m = new RawMessage(timeStampNs, message);
                Message pm = MessageParser.parse(m);
                String CSI = "\u001B[";
                String CLEAR_SCREEN = CSI + "2J";
                System.out.print(CLEAR_SCREEN);
                System.out.printf("%-6s %-10s %-10s %-20s %-10s %-10s %-7s %-7s \n", "ICAO", "CALLSIGN", "REG", "MODEL", "LONG", "LAT", "ALT", "SPD");
                if (pm != null) {
                    for (ObservableAircraftState a : asm.states()) {
                        if (!order.contains(a)) {
                            order.add(a);
                        }
                    }
                    asm.updateWithMessage(pm);
                    asm.purge();
                    AddressComparator comp = new AddressComparator();
                    order.sort(comp);

                    if (i == 0) {
                        for (ObservableAircraftState o : order) {
                            if (!o.getTrajectory().isEmpty()) {
                                printM(o);
                                i++;
                            }
                        }
                    } else {
                        for (ObservableAircraftState o : order) {
                            if (o.getTrajectory().size() > 0) {
                                printM(o);
                                i++;
                            }
                        }
                        //}
                    }
                }

                //System.out.printf("%13d: %s\n", timeStampNs, message);
                previousOrder = order;
            }
        } catch (
                EOFException e) { /* nothing to do */ }
    }

    public static void printM(ObservableAircraftState o) {
        String icao = o.getIcaoAddress().string();
        String callSign = (o.getCallSign() != null) ? o.getCallSign().string() : "";
        String regis = (o.getRegistration() != null) ? o.getRegistration().string() : "";
        String model = (o.getModel().length() <= 19) ? o.getModel() : o.getAircraftData().model().substring(0, 16) + "...";
        double velocity0 = Units.convert(o.getVelocity(), Units.Speed.METER_PER_SECOND, Units.Speed.KILOMETER_PER_HOUR);
        System.out.printf("%-6s %-10s %-10s %-20s %-10f %-10f %-7d %-7d \n", icao, callSign, regis, model, Units.convert(o.getPosition().longitude(), Units.Angle.RADIAN, Units.Angle.DEGREE), Units.convert(o.getPosition().latitude(), Units.Angle.RADIAN, Units.Angle.DEGREE), (int) Math.rint(o.getAltitude()), (int) Math.rint(velocity0));
    }

    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.getIcaoAddress().string();
            String s2 = o2.getIcaoAddress().string();
            return s1.compareTo(s2);
        }
    }
}