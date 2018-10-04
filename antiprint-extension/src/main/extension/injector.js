function injectProjection(projection) {

    const MODE_ALWAYS_REDEFINE = 'redefine';
    const MODE_REDEFINE_IF_PRESENT = 'maybeRedefine';

    class PropertySpec {
        constructor(name, mode) {
            this.name = name;
            this.mode = mode || MODE_ALWAYS_REDEFINE;
        }

        shouldDefine(navigator) {
            return this.mode === MODE_ALWAYS_REDEFINE || (this.mode === MODE_REDEFINE_IF_PRESENT && this.name in navigator);
        }
    }

    const PROJECTED_NAVIGATOR_PROPERTIES = [
        new PropertySpec('platform'),
        new PropertySpec('oscpu', MODE_REDEFINE_IF_PRESENT),
        new PropertySpec('buildID', MODE_REDEFINE_IF_PRESENT),
        new PropertySpec('appVersion', MODE_REDEFINE_IF_PRESENT),
    ];
    const NOOP = x => {};

    const previous = {};

    PROJECTED_NAVIGATOR_PROPERTIES.forEach(propSpec => {
        if (propSpec.shouldDefine(navigator)) {
            const property = propSpec.name;
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
                console.debug('injectProjection: error defining navigator property', property, err);
            }
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
