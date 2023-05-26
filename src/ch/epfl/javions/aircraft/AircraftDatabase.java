package ch.epfl.javions.aircraft;

import java.io.*;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class AircraftDatabase {

    private final String fileName;

    /**
     * Creates a database from a file name
     *
     * @param fileName file name containing the data
     * @throws NullPointerException whenever fileName is null
     */
    public AircraftDatabase(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    /**
     * @param address Address of the
     * @return AircraftData object storing the specifications of the Aircraft.
     * Searches specification of the aircraft using its id address.
     * @throws IOException if error while streaming through the file
     */

    public AircraftData get(IcaoAddress address) throws IOException {

        String l;
        String d = URLDecoder.decode(this.fileName, UTF_8);
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
                    new AircraftDescription(extractedLine[4])
                    , WakeTurbulenceCategory.of(extractedLine[5]));
        }
    }

    /**
     * @param address Address identification of the aircraft
     * @return .csv path according to the last two characters of the address string.
     * Gives the .csv path from the IcaoAddress.
     */
    private String getCsvFile(IcaoAddress address) {
        int endIndex = address.string().length();
        int startIndex = endIndex - 2;
        return address.string().substring(startIndex, endIndex) + ".csv";
    }

}

