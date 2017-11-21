'use strict';

if (typeof chrome.privacy['IPHandlingPolicy'] !== 'undefined') {
    window.IPHandlingPolicy = chrome.privacy['IPHandlingPolicy'];
    console.info("chrome.privacy.IPHandlingPolicy is already defined", window.IPHandlingPolicy);
}

if (typeof window.IPHandlingPolicy === 'undefined') {

    window.IPHandlingPolicy = {
        DEFAULT: 'default',
        DEFAULT_PUBLIC_AND_PRIVATE_INTERFACES: 'default_public_and_private_interfaces',
        DEFAULT_PUBLIC_INTERFACE_ONLY: 'default_public_interface_only',
        DISABLE_NON_PROXIED_UDP: 'disable_non_proxied_udp'
    };

}

/**
 * String constant policy value corresponding to 'disable_non_proxied_udp'. As described by the
 * WebRTC Network Limiter extension options page, "This option forces Chrome to use the
 * same network path for media as for normal web traffic, including use of a web proxy.
 * Chrome will always attempt to send media through the proxy, which will typically hurt
 * media performance and increase the load on the proxy; furthermore, this behavior may
 * be incompatible with some applications."
 *
 * This means if you configured Chrome to use a proxy, then it will use that proxy for WebRTC activity.
 * @type {string}
 */
const GOOD_POLICY = window.IPHandlingPolicy.DISABLE_NON_PROXIED_UDP;

(function(policy) {

    chrome.privacy.network.webRTCIPHandlingPolicy.set({
        value: policy
    });

})(GOOD_POLICY);

