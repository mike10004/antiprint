# antiprint

Antiprint is a Chrome extension that tries to make your browser seem less
unique to websites that you visit.

This extension is neither complete nor correct in achieving that goal. To
suggest ways to improve it, please open an issue.

## Coverage Areas

### User-Agent and navigator.platform conflicts

You can set your Chrome user agent using a command line option, but this does
not affect the read-only `navigator.platform` property. So if your browser
has JavaScript enabled, then a page is able to tell the server what your actual
platorm is, which may conflict with what your user agent indicates.

The question of how to modify `navigator.platform` was posed to the Chromium
Google Group [a few years ago](https://groups.google.com/a/chromium.org/forum/#!topic/chromium-discuss/8cCllrVX4kI),
and the best answer was to make an extension.

Somebody on StackOverflow [tried that](https://stackoverflow.com/questions/38808968)
but ran into some trouble. By using a technique described in an [answer to a different
question](https://stackoverflow.com/questions/9515704/insert-code-into-the-page-context-using-a-content-script/9517879#9517879),
they were reportedly able to make it work.

This extension attempts to implement the functionality of changing the value
of the `navigator.platform` property so that it is consistent with the user
agent. A future release may support manual specification of the `platform` value.

## Caveats

This extension is incompatible with some other extensions that affect a browser's
fingerprint. Specifically, errors may occur when this extension is enabled alongside
other extensions that define properties on `window.navigator` without allowing those
definitions to be overwritten. An example of this is the excellent 
[User-Agent Switcher for Chrome](https://chrome.google.com/webstore/detail/user-agent-switcher-for-c/djflhoibgkdhkhhcedjiklpkjnoahfmg). 

## Acknowledgments

Icon: [finger print](https://thenounproject.com/term/finger-print/183380/) by romzicon from the Noun Project
