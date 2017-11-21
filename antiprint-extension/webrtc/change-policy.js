'use strict';

/**
 * Integer policy value corresponding to 'disable_non_proxied_udp'. As described by the
 * WebRTC Network Limiter extension options page, "This option forces Chrome to use the
 * same network path for media as for normal web traffic, including use of a web proxy.
 * Chrome will always attempt to send media through the proxy, which will typically hurt
 * media performance and increase the load on the proxy; furthermore, this behavior may
 * be incompatible with some applications."
 *
 * This means if you configured Chrome to use a proxy, then it will use that proxy for WebRTC activity.
 * @type {number}
 */
const GOOD_POLICY = window.IPHandlingPolicy.DISABLE_NON_PROXIED_UDP.value;

(function(policy) {

    function isPolicyThatExposesIp(policy) {
        return window.IPHandlingPolicy.DEFAULT.equals(policy)
            || window.IPHandlingPolicy.DEFAULT_PUBLIC_AND_PRIVATE_INTERFACES.equals(policy);
    }

    chrome.privacy.network.webRTCIPHandlingPolicy.get({}, function(details) {
        if (isPolicyThatExposesIp(details.value)) {
            console.info("setting ip webRTCIPHandlingPolicy to " + window.IPHandlingPolicy.DISABLE_NON_PROXIED_UDP.name);
            chrome.privacy.network.webRTCIPHandlingPolicy.set({
                value: policy
            });
        } else {
            console.info("user already has webRTCIPHandlingPolicy set to something private");
        }
    });
})(GOOD_POLICY);

