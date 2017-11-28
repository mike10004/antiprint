package io.github.mike10004.extensibleffdriver;

/**
 * Class that represents the parameters for an addon installation.
 */
public class AddonUninstallation {

    /**
     * The addon id.
     */
    public final String id;

    public AddonUninstallation(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddonUninstallation that = (AddonUninstallation) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AddonUninstallation{" +
                "id='" + id + '\'' +
                '}';
    }
}
