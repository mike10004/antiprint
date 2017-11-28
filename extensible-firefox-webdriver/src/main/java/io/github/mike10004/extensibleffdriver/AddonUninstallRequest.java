package io.github.mike10004.extensibleffdriver;

import java.util.Map;
import java.util.Objects;

/**
 * Class that represents the parameters for uninstallation of an addon.
 */
public class AddonUninstallRequest {

    /**
     * The addon id.
     */
    private final String id;

    /**
     * Constructs a new request instance.
     * @param id the addon id; probably the value of the manifest {@code applications.gecko.id} field
     */
    private AddonUninstallRequest(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public static AddonUninstallRequest fromId(String id) {
        return new AddonUninstallRequest(id);
    }

    public void toParameters(Map<String, Object> params) {
        params.put("id", id);
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
