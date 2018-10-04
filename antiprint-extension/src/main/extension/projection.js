function SignatureCrafter(settings) {

    const KNOWN_LINUXEN = ['Ubuntu', 'Debian'];
    const MAKE_EMPTY = ['oscpu', 'appVersion', 'buildID'];
    const userAgent = (settings || {})['userAgent'] || window.navigator.userAgent;
    const signature = UAParser(userAgent);
    const navigatorProjection = {};
    navigatorProjection.platform = constructPlatform(signature);
    MAKE_EMPTY.forEach(property => navigatorProjection[property] = '');

    function mapArch(arch, osName) {
        if (osName === 'Linux' && arch === 'amd64') {
            return 'x86_64';
        }
        return arch;
    }

    function mapOsName(os) {
        let osName = os.name || '';
        if (KNOWN_LINUXEN.indexOf(osName) !== -1) {
            osName = 'Linux';
        }
        return osName;
    }

    function constructPlatform(signature) {
        const osName = mapOsName(signature.os);
        switch (osName) {
            case 'Windows':
                return 'Win32';
            case 'Mac OS':
                return 'MacIntel';
            default:
                return osName + ' ' + mapArch(signature.cpu.architecture, osName);
        }
    }

    this.navigator = function () {
        return navigatorProjection;
    };

}

