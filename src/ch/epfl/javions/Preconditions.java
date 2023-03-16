package ch.epfl.javions;
public final class Preconditions {
    private Preconditions(){}

    /**
     *
     * @param shouldBeTrue Statement of which we know the truth value.
     */
    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}




