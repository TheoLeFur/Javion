package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;

public final class MapParameters {

    Property<Integer> zoom;
    Property<Double> minX;
    Property<Double> minY;

    public MapParameters(
            int zoom,
            double minX,
            double minY
            )

    {
        Preconditions.checkArgument(
                zoom >= 6 &&
                zoom < 20
        );

        this.zoom = new
        this.minX = new ReadOnlyDoubleProperty(minX)
        this.minY = new ReadOnlyDoubleProperty(minY);
    }


    public void scroll(double newX, double newY){}
    public void changeZoomLevel(int zoom){
        Preconditions.checkArgument(
                zoom >= 6 &&
                        zoom < 20
        );
        this.zoom = new
    }
}
