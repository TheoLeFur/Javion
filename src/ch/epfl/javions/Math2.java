package ch.epfl.javions;

public class Math2 {

    private Math2() {
    }

    /**
     * Clips the value of an integer to minValue and maxValue
     *
     * @param value    Integer to be clipped
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @return Clipped Value
     * @author Theo Le Fur SCIPER : 363294
     */
    public static int clamp(int value, int minValue, int maxValue) {
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
     * @author Theo Le Fur SCIPER : 363294
     */
    public static double asinh(double value) {
        return Math.log(value + Math.sqrt(1 + Math.pow(value, 2)));
    }

}
