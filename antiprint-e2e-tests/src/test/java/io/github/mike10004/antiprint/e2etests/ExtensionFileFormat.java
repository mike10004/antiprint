package io.github.mike10004.antiprint.e2etests;

public enum ExtensionFileFormat {
    CRX, ZIP;

    public String suffix() {
        return "." + name().toLowerCase();
    }
}
