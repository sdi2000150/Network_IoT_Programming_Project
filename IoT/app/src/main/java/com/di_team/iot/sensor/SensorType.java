package com.di_team.iot.sensor;

import androidx.annotation.NonNull;


public enum SensorType {
    Smoke(0.0f, 0.25f, 0.14f),
    Gas(0.0f, 11.0f, 9.15f),
    Temperature(-5.0f, 80.0f, 50.0f),
    UV(0.0f, 11.0f, 6.0f);

    private final SensorLimits limits;

    SensorType(float min, float max, float threshold) {
        this.limits = new SensorLimits(min, max, threshold);
    }

    public static SensorType fromString(String type) {
        switch(type) {
            case "Smoke":
                return Smoke;
            case "Gas":
                return Gas;
            case "Temperature":
                return Temperature;
            default:
                return UV;
        }
    }

    public SensorLimits getSensorLimits() {
        return limits;
    }

    // Nested class
    public static class SensorLimits {
        private final float min;
        private final float max;
        private final float threshold;

        public SensorLimits(float min, float max, float threshold) {
            this.min = min;
            this.max = max;
            this.threshold = threshold;
        }

        public float getMin() {
            return min;
        }

        public float getMax() {
            return max;
        }

        public float getThreshold() {
            return threshold;
        }

        @NonNull
        @Override
        public String toString() {
            return "SensorLimits{" +
                    "min=" + min +
                    ", max=" + max +
                    ", threshold=" + threshold +
                    '}';
        }
    }
}

