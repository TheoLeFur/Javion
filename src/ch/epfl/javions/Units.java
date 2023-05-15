package ch.epfl.javions;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class Units {

    // mili prefix
    public static final double MILI = 1e-3;

    // Centi prefix
    public static final double CENTI = 1e-2;

    // Kilo prefix
    public static final double KILO = 1e3;

    /**
     * Private constructor, makes the class non instantiatable
     */
    private Units() {
    }

    /**
     * @param value    Value we want to convert
     * @param fromUnit Unit we convert from
     * @param toUnit   Unit we convert to
     * @return Converted Unit
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * fromUnit / toUnit;
    }

    /**
     * @param value    Value we want to convert
     * @param fromUnit Unit we convert from
     * @return Converted Unit
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    /**
     * @param value  Value we want to convert
     * @param toUnit Unit we convert to
     * @return Converted Unit
     */
    public static double convertTo(double value, double toUnit) {
        return value / toUnit;
    }

    public final static class Angle {


        // Fundamental angle unit ; radian
        public static final double RADIAN = 1;

        // Corresponds to one whole turn through a circle
        public static final double TURN = 2 * Math.PI * RADIAN;

        // Degrees, from 0 to 360
        public static final double DEGREE = TURN / 360;
        // Useful unit for working on Open Street Map, where we have varying zoom levels.
        public static final double T32 = TURN / Math.pow(2, 32);

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Angle() {
        }
    }

    public final static class Length {

        // 1 meter
        public static final double METER = 1;
        // 1 centimeter
        public static final double CENTIMETER = CENTI * METER;
        // 1 inch
        public static final double INCH = 2.54 * CENTIMETER;
        // 1 foot
        public static final double FOOT = 12 * INCH;
        // 1 kilometer
        public static final double KILOMETER = KILO * METER;
        // 1 nautical mile
        public static final double NAUTICAL_MILE = 1852 * METER;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Length() {
        }

    }

    public final static class Time {


        //1 second
        public static final double SECOND = 1;
        public static final double MILISECOND = SECOND * MILI;
        // 1 minute
        public static final double MINUTE = 60 * SECOND;
        // 1 hour
        public static final double HOUR = 3600 * SECOND;

        //added nanosecond unit of time measurement for AircraftStateManager
        public static final double NANO_SECOND = 1e-9 * SECOND;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Time() {
        }

    }

    public final static class Speed {

        // 1 m/s
        public static final double METER_PER_SECOND = Length.METER / Time.SECOND;
        // 1 km/h
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        // 1 nm/h
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Speed() {
        }

    }

}
