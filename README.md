[![Travis build status](https://img.shields.io/travis/mike10004/selenium-help.svg)](https://travis-ci.org/mike10004/antiprint)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/antiprint-extension.svg)](https://repo1.maven.org/maven2/com/github/mike10004/antiprint-extension/)

# antiprint

Antiprint is an extension for Chrome and Firefox that makes your browser appear
less unique to websites that you visit.

This extension is neither complete nor correct in accomplishing that feat. To
suggest ways to improve it, please open an issue.

## Coverage Areas

### User-Agent and navigator.platform conflicts

You can set your Chrome user agent using a command line option, but this does
not affect the read-only `navigator.platform` property. So if your browser
has JavaScript enabled, then a page is able to tell the server what your actual
platorm is, which may conflict with what your user agent indicates.

The question of how to modify `navigator.platform` was posed to the Chromium
Google Group [a few years ago][google-group-question], and the best answer was 
to make an extension.

Somebody on StackOverflow [tried that](https://stackoverflow.com/questions/38808968)
but ran into some trouble. By using a technique described in an [answer to a different
question][helpful-so-answer], they were reportedly able to make it work.

This extension causes `navigator.platform` to report a value that it is 
consistent with the user agent. A future release may support manual 
specification of the `platform` value. To be clear, this extension does not 
allow you to specify a custom value for `navigator.platform`. It automatically 
selects an appropriate `navigator.platform` value for the user agent string 
that is configured for the browser.

## WebRTC private network IP leakage

In Chrome, by default, a script could learn your private network IP address by executing
code that facilitates good WebRTC (video chat) connections. This extension protects
against that by changing that setting to a setting that is potentially less efficient
but only allows scripts access to the public IP address, using code from the 
[WebRTC Network Limiter extension][chrome-webrtc-network-limiter]. 

In theory, this should work for Firefox too, but in practice, you should just set 
the value of the preference `media.peerconnection.enabled` to false.

## Caveats

### Incompatible with some other Chrome extensions

This extension is incompatible with some other extensions that affect a browser's
fingerprint. Specifically, errors may occur when this extension is enabled alongside
other extensions that define properties on `window.navigator` without allowing those
definitions to be overwritten. An example of such an extension that is unfortunately
incompatible is the excellent [User-Agent Switcher for Chrome][chrome-ua-switcher]. 

### Firefox webdriver support

The `extensible-firefox-webdriver` artifact provides an implementation of a Firefox
webdriver that supports installing unsigned extensions. This is a feature in the
webdriver spec that is not yet supported by the Selenium Java library (as of version 3.7.1).
The naming convention for the artifact version is `A.B.CxD.E`, where `A.B.C` is the 
Selenium Java version and `D.E` is the Antiprint extension version. For example,
the version compatible with Selenium Java 3.7.1 is `3.7.1x0.6`.  

## Acknowledgments

* The extension icon is [finger print](https://thenounproject.com/term/finger-print/183380/) 
  by romzicon from the Noun Project

[google-group-question]: https://groups.google.com/a/chromium.org/forum/#!topic/chromium-discuss/8cCllrVX4kI
[helpful-so-answer]: https://stackoverflow.com/questions/9515704/insert-code-into-the-page-context-using-a-content-script/9517879#9517879
[chrome-ua-switcher]: https://chrome.google.com/webstore/detail/user-agent-switcher-for-c/djflhoibgkdhkhhcedjiklpkjnoahfmg
[chrome-webrtc-network-limiter]: https://chrome.google.com/webstore/detail/webrtc-network-limiter/npeicpdbkakmehahjeeohfdhnlpdklia

