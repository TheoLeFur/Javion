package ch.epfl.javions.gui;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;


/**
 * Class for representing a continuous spectrum of colors we will use in our graphic interface.
 */
public final class ColorRamp {

    private final List<Color> colorList;
    // PLASMA ColorRamp we will use for the rest of the project.
    public static final ColorRamp PLASMA = new ColorRamp(List.of(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"))
    );


    /**
     * Create a lattice of colors, specified by the colorList. One can then interpolate between the lattices
     * to obtain a continuous spectrum of colors, using the method Color at(double idx)
     *
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
        int N = this.colorList.size();
        if (index < 0) {
            return this.colorList.get(0);
        } else if (index > 1) {
            return this.colorList.get(N - 1);
        } else {
            double colorIndex = index * (N - 1);
            int c0Index = (int) Math.floor(colorIndex);
            int c1Index = (int) Math.ceil(colorIndex);
            Color c0 = this.colorList.get(c0Index);
            Color c1 = this.colorList.get(c1Index);
            double c1Prop = c1Index - colorIndex;
            return c0.interpolate(c1, c1Prop);
        }
    }
}

