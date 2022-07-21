package com.copiloto;
import android.location.Location;
import android.util.Log;

public class CLocation extends Location {

    private boolean calculation = false;

    public CLocation(Location location) {
        super(location);
    }

    public boolean allowCalculation() {
        return this.calculation;
    }

    public void setAllowCalculation(boolean calculation){
        this.calculation = calculation;
    }

    @Override
    public float getSpeed() {
        // Speed meters/second to kilometer/hour
        float nSpeed = 0.0f;
        if(this.allowCalculation()) {
            nSpeed = super.getSpeed() * 3.6f;
        }
        return nSpeed;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);

        if(!this.allowCalculation()) {
            // Convert meters to feet
            nDistance = nDistance * 3.28083989501312f;
        }
        return nDistance;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();

        if(!this.allowCalculation()) {
            // Convert meters to feet
            nAltitude = nAltitude * 3.28083989501312f;
        }
        return nAltitude;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();

        if(!this.allowCalculation()) {
            // Convert meters to feet
            nAccuracy = nAccuracy * 3.28083989501312f;
        }
        return nAccuracy;
    }
}
