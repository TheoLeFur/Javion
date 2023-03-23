package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double velocity,
                                      double trackOrHeading) implements Message {

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(
                timeStampNs >= 0 &&
                        velocity >= 0 &&
                        trackOrHeading >= 0

        );
    }

    /**
     * Creates a velocity message from a raw message.
     * @param rawMessage Raw Message.
     * @return instance of Velocity Message.
     */
    AirborneVelocityMessage of(RawMessage rawMessage) {

        if (rawMessage.typeCode() == 19) {

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
                int vew = Bits.extractUInt(information, 21, 10);

                vx = vew - 1;
                vy = vns - 1;

                int sgnEW = 2 * dew - 1;
                int sgnNS = 1 - 2 * dns;

                if (dns == 0 || vew == 0) {
                    return null;
                } else {
                    trackOrHeading = Units.convertTo(Math.atan2(sgnNS * vy, sgnEW * vx), Units.Angle.DEGREE);
                    if (subType == 1) {
                        velocity = Units.convertFrom(Math.hypot(vx, vy), Units.Speed.KNOT);
                    } else {
                        velocity = 4 * Units.convertFrom(Math.hypot(vx, vy), Units.Speed.KNOT);
                    }

                    return new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading);
                }

            } else if (subType == 3 || subType == 4) {

                int sh = Bits.extractUInt(information, 21, 1);


                if (sh == 0) {
                    return null;
                } else {

                    int cap = Bits.extractUInt(information, 11, 10);
                    double capTours = cap / Math.pow(2, 10);
                    trackOrHeading = Units.convert(capTours, Units.Angle.TURN, Units.Angle.DEGREE);

                    int airVelocity = Bits.extractUInt(information, 0, 10);

                    if (subType == 3) {
                        velocity = Units.convertFrom(airVelocity, Units.Speed.KNOT);
                    } else {
                        velocity = 4 * Units.convertFrom(airVelocity, Units.Speed.KNOT);
                    }

                    return new AirborneVelocityMessage(timeStampNs, icaoAddress, velocity, trackOrHeading);
                }


            }
        }
        return null;
    }
}
