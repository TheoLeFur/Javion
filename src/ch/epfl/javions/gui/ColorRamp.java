package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;


/**
 * Class for representing a continuous spectrum of colors we will use in our graphic interface
 */
public final class ColorRamp {

    private final List<Color> colorList;

    /**
     * Create a lattice of colors, specified by the colorList
     * @param colorList list of colors we want to use as the base of our spectrum
     */
    public ColorRamp(List<Color> colorList) {
        this.colorList = Collections.unmodifiableList(colorList);
    }

    /**
     * Consider the spectrum of colours, append onto [0,1] given by the list of colors in the constructor.
     * We wish to make a continuous spectrum of colors out of this. For this reason, we choose an index between
     * 0 and 1 and create an interpolation between the two nearest colors from the list, neighboring the index in the spectrum.
     * This yields a continuous spectrum.
     *
     * @param index index clipped to the interval [0,1]
     * @return a color resulting from the interpolation of the lattice of colors given in the constructor.
     */
    public Color at(double index) {
        index = Math2.clamp(0, index, 1);
        double colorIndex = index * (this.colorList.size() - 1);
        int c0Index = (int) Math.floor(colorIndex);
        int c1Index = (int) Math.ceil(colorIndex);
        Color c0 = this.colorList.get(c0Index);
        Color c1 = this.colorList.get(c1Index);
        double c1Prop = c1Index - colorIndex;
        return c0.interpolate(c1, c1Prop);
    }

}
