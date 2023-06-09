package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @param registration           registration id of aircraft
 * @param typeDesignator         type of aircraft
 * @param model                  model of aircraft
 * @param description            additional description of aircraft
 * @param wakeTurbulenceCategory category
 * @author Theo Le Fur (SCIPER: 363294)
 * Record tracking data of the aircraft. Parameters' name state what is being tracked.
 */

public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * @param registration           registration id of aircraft
     * @param typeDesignator         type of aircraft
     * @param model                  model of aircraft
     * @param description            additional description of aircraft
     * @param wakeTurbulenceCategory category
     * @throws NullPointerException whenever one of the arguments passed at construction is null.
     */
    public AircraftData {

        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);

    }
}
