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
public class TextUI {

    public static final String message_dir = "/Users/theolefur/Downloads/Javions/resources/messages_20230318_0915.bin";

    public static void main(String[] args) throws IOException, InterruptedException {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(message_dir)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            RawMessage m;
            AircraftDatabase test = new AircraftDatabase("/aircraft.zip");
            AircraftStateManager asm = new AircraftStateManager(test);
            long time = 0;
            int i = 0;
            for (int j = 0; j < 100; ++j) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                m = new RawMessage(timeStampNs, message);
                if (m != null) {
                    if (i == 0) {
                        asm.updateWithMessage(m);
                        time = timeStampNs;
                        ++i;
                        String CSI = "\u001B[";
                        String CLEAR_SCREEN = CSI + "2J";
                        System.out.print(CLEAR_SCREEN);
                        asm.purge();
                        List<ObservableAircraftState> order = new ArrayList<>();
                        order.addAll(asm.states());
                        AddressComparator comp = new AddressComparator();
                        order.sort(comp);
                        for (ObservableAircraftState o : order) {
                            String icao = o.getIcaoAddress().string();
                            String callSign = (o.getCallSign() != null) ? o.getCallSign().string() : "";
                            String regis = (o.getRegistration() != null) ? o.getRegistration().string() : "";
                            String model = (o.getModel().length() <= 19) ? o.getModel() : o.getModel().substring(0, 16) + "...";
                            double velocity0 = Units.convert(o.getVelocity(), Units.Speed.METER_PER_SECOND, Units.Speed.KILOMETER_PER_HOUR);
                            System.out.printf("%-6s %-8s %-6s %-20s %-10f %-10f %-7d %-7d", icao, callSign, regis, model, Units.convert(o.getPosition().longitude(), Units.Angle.RADIAN, Units.Angle.DEGREE), Units.convert(o.getPosition().latitude(), Units.Angle.RADIAN, Units.Angle.DEGREE), (int) Math.rint(o.getAltitude()), (int) Math.rint(velocity0));
                            System.out.println();
                        }
                    } else {
                        time = timeStampNs - time;
                        long timeMs = (long) (time * 1e-7);
                        sleep(timeMs);
                        asm.updateWithMessage(m);
                        String CSI = "\u001B[";
                        String CLEAR_SCREEN = CSI + "2J";
                        System.out.print(CLEAR_SCREEN);
                        asm.purge();
                        List<ObservableAircraftState> order = new ArrayList<>();
                        order.addAll(asm.states());
                        AddressComparator comp = new AddressComparator();
                        order.sort(comp);
                        //System.out.println(order.size());
                        for (ObservableAircraftState o : order) {
                            String icao = o.getIcaoAddress().string();
                            String callSign = (o.getCallSign() != null) ? o.getCallSign().string() : "";
                            String regis = (o.getRegistration() != null) ? o.getRegistration().string() : "";
                            String model = (o.getModel().length() <= 19) ? o.getModel() : o.getModel().substring(0, 16) + "...";
                            double velocity0 = Units.convert(o.getVelocity(), Units.Speed.METER_PER_SECOND, Units.Speed.KILOMETER_PER_HOUR);

                            if (o.getTrajectory().size() > 0) {
                                System.out.printf("%-6s %-8s %-6s %-20s %-10f %-10f %-7d %-7d", icao, callSign, regis, model,
                                        Units.convert(o.getTrajectory().get(o.getTrajectory().size() - 1).position().longitude(),
                                                Units.Angle.RADIAN, Units.Angle.DEGREE),
                                        Units.convert(o.getTrajectory().get(o.getTrajectory().size() - 1).position().latitude(),
                                                Units.Angle.RADIAN, Units.Angle.DEGREE),
                                        (int) Math.rint(o.getTrajectory().get(o.getTrajectory().size() - 1).altitude()), (int) Math.rint(velocity0));
                                System.out.println();
                            }
                        }
                    }
                }
                //System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        } catch (EOFException e) { /* nothing to do */ }
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
