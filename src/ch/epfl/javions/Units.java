package ch.epfl.javions;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class Units {


    public static final double CENTI = 1e-2;
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

    public static class Angle {

        public static final double RADIAN = 1;
        public static final double TURN = 2 * Math.PI * RADIAN;
        public static final double DEGREE = TURN / 360;
        public static final double T32 = TURN / Math.pow(2, 32);

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Angle() {
        }
    }

    public static class Length {

        public static final double METER = 1;
        public static final double CENTIMETER = CENTI * METER;
        public static final double INCH = 2.54 * CENTIMETER;
        public static final double FOOT = 12 * INCH;
        public static final double KILOMETER = KILO * METER;
        public static final double NAUTICAL_MILE = 1852 * METER;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Length() {
        }

    }

    public static class Time {

        public static final double SECOND = 1;
        public static final double MINUTE = 60 * SECOND;
        public static final double HOUR = 3600 * SECOND;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Time() {
        }

    }

    public static class Speed {

        public static final double METER_SECOND = Length.METER / Time.SECOND;
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;

        /**
         * Private constructor, makes the class non instantiatable
         */
        private Speed() {
        }

    }

}
