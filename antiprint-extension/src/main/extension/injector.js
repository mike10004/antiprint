function injectProjection(projection) {

    const PROJECTED_NAVIGATOR_PROPERTIES = ['platform'];
    const NOOP = x => {};

    const previous = {};
    PROJECTED_NAVIGATOR_PROPERTIES.forEach(property => {
        previous[property] = navigator[property];
        try {
            Object.defineProperty(navigator, property, {
                get: function () {
                    return projection.navigator[property];
                },
                set: NOOP,
                configurable: true
            });
        } catch (err) {
            console.debug('platform-reform: error defining navigator property', property, err);
        }
    });

}

(function(){
    const projection = {};
    projection.navigator = new SignatureCrafter({
        'userAgent': window.navigator.userAgent
    }).navigator();
    const scriptEl = document.createElement('script');
    scriptEl.textContent = "(" + injectProjection.toString() + ")(" + JSON.stringify(projection) + ");";
    const docHead = document.head || document.documentElement;
    docHead.appendChild(scriptEl);
    scriptEl.remove();
})();
