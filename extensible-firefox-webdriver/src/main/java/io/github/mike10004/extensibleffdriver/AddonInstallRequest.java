package io.github.mike10004.extensibleffdriver;

import java.io.File;
import java.util.Objects;

/**
 * Class that represents the parameters for a request to install an addon.
 */
public class AddonInstallRequest {

    /**
     * Pathname of the zip file containing the addon.
     */
    public final File zipPathname;

    /**
     * Duration the addon will persist. Temporary means for the current session only
     * and permanent means it will survive through a browser close/open cycle.
     */
    public final AddonPersistence persistence;

    /**
     * Constructs a request instance.
     * @param zipPathname pathname of the addon zip
     * @param persistence session persistence of the addon
     */
    public AddonInstallRequest(File zipPathname, AddonPersistence persistence) {
        this.zipPathname = Objects.requireNonNull(zipPathname, "zip pathname");
        this.persistence = Objects.requireNonNull(persistence, "duration");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddonInstallRequest that = (AddonInstallRequest) o;

        if (zipPathname != null ? !zipPathname.equals(that.zipPathname) : that.zipPathname != null) return false;
        if (persistence != that.persistence) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zipPathname != null ? zipPathname.hashCode() : 0;
        result = 31 * result + (persistence != null ? persistence.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AddonInstallation{" +
                "zipPathname=" + zipPathname +
                ", duration=" + persistence +
                '}';
    }
}
