package io.github.mike10004.extensibleffdriver;

import java.io.File;
import java.util.Objects;

public class AddonInstallRequest {

    public final File zipPathname;
    public final AddonPersistence duration;

    public AddonInstallRequest(File zipPathname, AddonPersistence duration) {
        this.zipPathname = Objects.requireNonNull(zipPathname, "zip pathname");
        this.duration = Objects.requireNonNull(duration, "duration");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddonInstallRequest that = (AddonInstallRequest) o;

        if (zipPathname != null ? !zipPathname.equals(that.zipPathname) : that.zipPathname != null) return false;
        if (duration != that.duration) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zipPathname != null ? zipPathname.hashCode() : 0;
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AddonInstallation{" +
                "zipPathname=" + zipPathname +
                ", duration=" + duration +
                '}';
    }
}
