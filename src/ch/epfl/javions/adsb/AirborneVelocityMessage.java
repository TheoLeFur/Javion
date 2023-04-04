package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(
                timeStampNs >= 0 &&
                        speed >= 0 &&
                        trackOrHeading >= 0

        );
    }

    /**
     * Creates an AirborneVelocityMessage out of a raw message.
     *
     * @param rawMessage Raw Message.
     * @return instance of Velocity Message.
     * @author Theo Le Fur SCIPER : 363294
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {


        long payload = rawMessage.payload();
        int subType = Bits.extractUInt(payload, 48, 3);
        int information = Bits.extractUInt(payload, 21, 22);
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        long timeStampNs = rawMessage.timeStampNs();

        double vx;
        double vy;
        double velocity;
        double trackOrHeading;

        if (subType == 1 || subType == 2) {

            int dew = Bits.extractUInt(information, 21, 1);
            int dns = Bits.extractUInt(information, 10, 1);
            int vns = Bits.extractUInt(information, 0, 10);
            int vew = Bits.extractUInt(information, 11, 10);

            if (vns == 0 || vew == 0) {
                return null;
            } else {
                double[] velocities = computeVelocityComponents(vns, vew, dns, dew);
                vx = velocities[0];
                vy = velocities[1];
                trackOrHeading = Math.atan2(vx, vy);
                if (trackOrHeading < 0) {
                    trackOrHeading = trackOrHeading + 2 * Math.PI;
                }
                if (subType == 1) {
                    velocity = Units.convertFrom(Math.hypot(vx, vy), Units.Speed.KNOT);
                } else {
                    velocity = Units.convertFrom(Math.hypot(vx, vy), 4 * Units.Speed.KNOT);
                }
                return new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading);
            }
        } else if (subType == 3 || subType == 4) {
            int sh = Bits.extractUInt(information, 21, 1);
            if (sh == 0) {
                return null;
            } else {
                double cap = Bits.extractUInt(information, 11, 10);
                double capTours = cap / (1 << 10);
                trackOrHeading = Units.convert(capTours, Units.Angle.TURN, Units.Angle.RADIAN);
                int as = Bits.extractUInt(information, 0, 10);
                if (as == 0) {
                    return null;
                } else {
                    double airVelocity = as - 1;
                    if (subType == 3) {
                        velocity = Units.convertFrom(airVelocity, Units.Speed.KNOT);
                    } else {
                        velocity = Units.convertFrom(airVelocity, Units.Speed.KNOT * 4);
                    }
                    return new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading);
                }
            }
        } else return null;
    }


    /**
     * @param vns North-South axis component of velocity + 1.
     * @param vew East-West axis component of velocity + 1.
     * @param dns 1 if the aircraft goes from North to South, 0 otherwise
     * @param dew 1 if the aircraft goes from East to West, 0 otherwise.
     * @return array containing the velocity components.
     * @author Theo Le Fur SCIPER : 363294
     * Auxiliary method for computing the velocity components, according to the direction of the aircraft.
     */

    private static double[] computeVelocityComponents(int vns, int vew, int dns, int dew) {
        int vx, vy;
        double[] velocityComponents = new double[2];
        if (dew == 0) {
            vx = vew - 1;
        } else {
            vx = -1 * (vew - 1);
        }
        if (dns == 0) {
            vy = vns - 1;
        } else {
            vy = -1 * (vns - 1);
        }
        velocityComponents[0] = vx;
        velocityComponents[1] = vy;

        return velocityComponents;
    }


}

