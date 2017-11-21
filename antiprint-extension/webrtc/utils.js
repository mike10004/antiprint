/*
 *  Copyright (c) 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree.
 */

'use strict';

if (typeof window.IPHandlingPolicy === 'undefined') {

    const Constant = function(name, value) {
        this.name = name;
        this.value = value;

        this.equals = function(other) {
            return (name === other) || (value === other);
        };
    };

    window.IPHandlingPolicy = {};
    window.IPHandlingPolicy.DEFAULT = new Constant('default', 0);
    window.IPHandlingPolicy.DEFAULT_PUBLIC_AND_PRIVATE_INTERFACES = new Constant('default_public_and_private_interfaces', 1);
    window.IPHandlingPolicy.DEFAULT_PUBLIC_INTERFACE_ONLY = new Constant('default_public_interface_only', 2);
    window.IPHandlingPolicy.DISABLE_NON_PROXIED_UDP = new Constant('disable_non_proxied_udp', 3);
}

