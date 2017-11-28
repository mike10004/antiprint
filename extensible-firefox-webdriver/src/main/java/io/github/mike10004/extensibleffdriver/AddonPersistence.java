package io.github.mike10004.extensibleffdriver;

/**
 * Enumeration of addon persistence properties.
 */
public enum AddonPersistence {

    /**
     * Addon will be installed for this browser session only.
     */
    TEMPORARY,

    /**
     * Addon will persist until uninstalled.
     */
    PERMANENT
}
