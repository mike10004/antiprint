function PlatformReformer() {
  let signature;

  function mapArch(arch, osName) {
    if (osName === 'Linux' && arch === 'amd64') {
      return 'x86_64';
    }
    return arch;
  }

  const KNOWN_LINUXEN = ['Ubuntu', 'Debian'];

  function constructPlatform(signature) {
    let osName = signature.os.name || '';
    if (KNOWN_LINUXEN.indexOf(osName) !== -1) {
      osName = 'Linux';
    }
    switch (osName) {
      case 'Windows':
        return 'Win32';
      case 'Mac OS':
        return 'MacIntel';
      default:
        return osName + ' ' + mapArch(signature.cpu.architecture, osName);
    }
  }

  this.parseUserAgentSignature = function (userAgent) {
    if (typeof signature === 'undefined') {
      signature = UAParser(userAgent);
      signature.platform = constructPlatform(signature);
    }
    return signature;
  }
}

(function() {

  const platformReformer = new PlatformReformer();
  const REFORMED_PROPERTIES = ['platform'];
  const NOOP = x => {};

  /**
   * Returns the signature property name corresponding to a given navigator
   * property name.
   */
  function mapProperty(navigatorProperty) {
    // No different names yet
    return navigatorProperty;
  }

  const previous = {};
  REFORMED_PROPERTIES.forEach(property => {
    previous[property] = navigator[property];
    try {
      Object.defineProperty(navigator, property, {
        get: function () {
          if (window.PlatformReformSettings && window.PlatformReformSettings.disabled) {
            return previous[property];
          } else {
            const signature = platformReformer.parseUserAgentSignature(navigator.userAgent);
            const signatureProperty = mapProperty(property);
            return signature[signatureProperty];
          }
        },
        set: NOOP,
        configurable: true
       });
    } catch (err) {
      console.debug('platform-reform: error defining navigator property', property, err);
    }
  });

})();
