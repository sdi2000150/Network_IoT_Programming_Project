package data_util;

public enum Severity {
    None, Moderate, High;

    public boolean isMoreSevereThan(Severity other) {
        return this.compareTo(other) > 0;
    }
}
