package ch.epfl.javions;

public final class Preconditions {
    private Preconditions() {
    }

    /**
     * An auxiliary method for verifying the validity of parameters.
     *
     * @param shouldBeTrue Statement of which we know the truth value.
     * @author Theo Le Fur SCIPER : 363294
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }


}




