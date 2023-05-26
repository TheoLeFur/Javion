package ch.epfl.javions;

/**
 * @author Theo Le Fur (SCIPER: 363294)
 */
public final class Preconditions {

    /**
     * Private constructor, makes the class non instantiatable
     */
    private Preconditions() {
    }

    /**
     * An auxiliary method for verifying the validity of parameters.
     *
     * @param shouldBeTrue Statement of which we know the truth value.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }


}




