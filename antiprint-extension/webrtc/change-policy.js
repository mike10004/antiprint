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
const GOOD_POLICY = chrome.privacy.IPHandlingPolicy.DISABLE_NON_PROXIED_UDP;

(function(policy) {

    function isPolicyThatExposesIp(policy) {
        return policy === chrome.privacy.IPHandlingPolicy.DEFAULT
            || policy === chrome.privacy.IPHandlingPolicy.DEFAULT_PUBLIC_AND_PRIVATE_INTERFACES;
    }

    if (browserSupportsIPHandlingPolicy()) {
        pn.webRTCIPHandlingPolicy.get({}, function(details) {
            if (isPolicyThatExposesIp(details.value)) {
                chrome.privacy.network.webRTCIPHandlingPolicy.set({
                    value: policy
                });
            }
        });
    } else {
        throw 'browser must support IPHandlingPolicy';
    }
})(GOOD_POLICY);

