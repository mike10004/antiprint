package io.github.mike10004.extensibleffdriver;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Class that represents the parameters for a request to install an addon.
 */
public interface AddonInstallRequest {

    /**
     * Populates a request parameters map as required for this installation request.
     * @param parameters the parameters map
     */
    void toParameters(Map<String, Object> parameters);

    /**
     * Constructs a request instance.
     * @param zipPathname pathname of the addon zip
     * @param persistence session persistence of the addon
     */
    static AddonInstallRequest fromFile(File zipPathname, AddonPersistence persistence) {
        Objects.requireNonNull(zipPathname, "zip pathname");
        Objects.requireNonNull(persistence, "persistence");
        return new AddonInstallRequest() {
            @Override
            public void toParameters(Map<String, Object> params) {
                params.put("path", zipPathname.getAbsolutePath());
                params.put("temporary", persistence == AddonPersistence.TEMPORARY);
            }
        };
    }

    static AddonInstallRequest fromBase64(String zipBytesBase64, AddonPersistence persistence) {
        Objects.requireNonNull(persistence, "persistence");
        Objects.requireNonNull(zipBytesBase64, "zip bytes base-64");
        checkArgument(!zipBytesBase64.isEmpty(), "zip bytes must be nonempty");
        return new AddonInstallRequest() {
            @Override
            public void toParameters(Map<String, Object> parameters) {
                parameters.put("addon", zipBytesBase64);
                parameters.put("temporary", persistence == AddonPersistence.TEMPORARY);
            }
        };
    }

}
