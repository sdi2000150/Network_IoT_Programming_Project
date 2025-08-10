package data_util;

/**Location of an object, coordinates are given in DEGREES*/
public class Location {
    private float longitude;
    private float latitude;

    public Location (float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public static Location fromString(String s) throws IllegalArgumentException {
        String[] parts = s.split(" ");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Given string has too few parts");
        }
        if (parts[0].equalsIgnoreCase("longitude") && parts[2].equalsIgnoreCase("latitude")) {
            return new Location(Float.parseFloat(parts[1]), Float.parseFloat(parts[3]));
        }
        else if (parts[0].equalsIgnoreCase("latitude") && parts[2].equalsIgnoreCase("latitude")) {
            return new Location(Float.parseFloat(parts[3]), Float.parseFloat(parts[1]));
        }
        else{
            throw new IllegalArgumentException("Labels don't match necessary arguments \"Longitude\" and \"Latitude\"");
        }
    }

    public boolean equals (Location other) {
        if (other == null) return false;
        return (this.longitude == other.longitude) && (this.latitude == other.latitude);
    }

    public String toString() {
        return "Longitude " + this.longitude + " Latitude " + this.latitude;
    }
}
