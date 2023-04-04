package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @param registration
 * @param typeDesignator
 * @param model
 * @param description
 * @param wakeTurbulenceCategory
 * @author : Theo Le Fur SCIPER : 363294
 * Record tracking data of the aircraft. Parameters' name state what is being tracked.
 */

public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    public AircraftData {

        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);

    }
}
