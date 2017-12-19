document.addEventListener('DOMContentLoaded', function() {
    const updatePage = function(details) {
        document.getElementById('webRTCIPHandlingPolicy').innerText = details.value;
    };
    const promise = chrome.privacy.network.webRTCIPHandlingPolicy.get({}, updatePage);
    if (promise instanceof Promise) {
        promise.then(updatePage);
    }
});
