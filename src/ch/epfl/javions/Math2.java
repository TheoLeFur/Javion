package ch.epfl.javions;

/**
 * @author Rudolf Yazbeck (SCIPER: 360700)
 * @author Theo Le Fur (SCIPER: 363294)
 */
public class Math2 {

    /**
     * Private constructor, makes the class non instantiatable
     */
    private Math2() {
    }

    /**
     * Clips the value of an integer to minValue and maxValue
     *
     * @param value    Integer to be clipped
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @return Clipped Value
     */
    public static int clamp(int minValue, int value, int maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException();
        } else if (value < minValue) {
            return minValue;
        } else return Math.min(value, maxValue);
    }

    /**
     * Inverse hyperbolic sine function
     *
     * @param value argument
     * @return image of value under asinh function
     */
    public static double asinh(double value) {
        return Math.log(value + Math.sqrt(1 + Math.pow(value, 2)));
    }

}
