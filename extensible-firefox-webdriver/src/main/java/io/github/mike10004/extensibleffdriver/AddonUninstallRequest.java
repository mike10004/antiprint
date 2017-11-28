package io.github.mike10004.extensibleffdriver;

/**
 * Class that represents the parameters for uninstallation of an addon.
 */
public class AddonUninstallRequest {

    /**
     * The addon id.
     */
    public final String id;

    /**
     * Constructs a new request instance.
     * @param id the addon id; probably the value of the manifest {@code applications.gecko.id} field
     */
    public AddonUninstallRequest(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddonUninstallRequest that = (AddonUninstallRequest) o;

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
