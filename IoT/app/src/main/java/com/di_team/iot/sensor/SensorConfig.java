package com.di_team.iot.sensor;

/**In-app sensor's configuration*/
public class SensorConfig {
    final private SensorType type;
    private boolean isActive;
    final private float minValue;
    final private float maxValue;
    private float progress;

    public SensorConfig(SensorType type, float minValue, float maxValue) {
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isActive = true;
        progress = (maxValue + minValue)/2; //initialize progress to middle
    }

    public SensorType getType() { return type; }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getProgress() { return progress; }

    public void setProgress(float p) { progress = p; }
}


