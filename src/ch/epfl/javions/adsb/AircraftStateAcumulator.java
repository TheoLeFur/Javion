package ch.epfl.javions.adsb;

import java.util.Objects;

public class AircraftStateAcumulator<T> {

    private T stateSetter;

    public AircraftStateAcumulator(T stateSetter){
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    /**
     * A getter for the state setter
     * @return state setter passed in the constructor.
     */
    public T stateSetter(){
        return this.stateSetter;
    }

    public void update(Message message){}
}
