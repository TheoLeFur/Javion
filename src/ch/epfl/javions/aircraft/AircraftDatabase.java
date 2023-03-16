package ch.epfl.javions.aircraft;

import java.io.*;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class AircraftDatabase {

    private final String fileName;

    public AircraftDatabase(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * @author Theo Le Fur
     * Searches specification of the aircraft using its id address.
     * @param address Address of the aircraft
     * @return AircraftData object storing the specifications of the Aircraft.
     */

    public AircraftData get(IcaoAddress address) throws IOException {

        String l;
        String d = Objects.requireNonNull(getClass().getResource(this.fileName)).getFile();
        d = URLDecoder.decode(d, UTF_8);
        try (ZipFile z = new ZipFile(d);
             InputStream s = z.getInputStream(z.getEntry(getCsvFile(address)));
             Reader r = new InputStreamReader(s, UTF_8);
             BufferedReader b = new BufferedReader(r)) {
            while ((l = b.readLine()) != null && !l.startsWith(address.string())) {
                continue;
            }

        }
        if (Objects.isNull(l)) return null;
        else {
            String[] extractedLine = l.split(",", -1);
            return new AircraftData(
                    new AircraftRegistration(extractedLine[1]),
                    new AircraftTypeDesignator(extractedLine[2]),
                    extractedLine[3],
                    new AircraftDescription(extractedLine[4]),
                    WakeTurbulenceCategory.of(extractedLine[5])
            );
        }
    }

    /**
     * @author Theo Le Fur
     * Gives the .csv path from the IcaoAddress.
     * @param address Address identification of the aircraft
     * @return .csv path according to the last two characters of the address string.
     */
    private String getCsvFile(IcaoAddress address) {
        int endIndex = address.string().length();
        int startIndex = endIndex - 2;
        return address.string().substring(startIndex, endIndex) + ".csv";
    }

}

