package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @param registration           registration id of aircraft
 * @param typeDesignator         type of aircraft
 * @param model                  model of aircraft
 * @param description            additional description of aircraft
 * @param wakeTurbulenceCategory category
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 * Record tracking data of the aircraft. Parameters' name state what is being tracked.
 */

public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * Record storing all the relevant data of an aircraft. Whenever one of the parameters is null
     * a NullPointerException is thrown
     */
    public AircraftData {

        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);

    }
}
