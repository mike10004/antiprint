function SignatureCrafter(settings) {

    const KNOWN_LINUXEN = ['Ubuntu', 'Debian'];
    const userAgent = (settings || {})['userAgent'] || window.navigator.userAgent;
    const signature = UAParser(userAgent);
    const navigatorProjection = {};
    navigatorProjection.platform = constructPlatform(signature);
    navigatorProjection.appVersion = constructAppVersion(signature);

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

    function mapFirefoxAppVersionParenthetical(signature) {
        const osName = mapOsName(signature.os);
        switch (osName) {
            case 'Mac OS':
                return 'Macintosh';
            case 'Linux':
                return 'X11';
            case 'Windows':
                return 'Windows';
        }
        // else return undefined
    }

    function parseMozillaVersion() {
        const match = /^Mozilla\/(\d+\.\d+)\s+.*$/.exec(userAgent);
        if (match) {
            return match[1];
        }
    }

    function constructFirefoxAppVersion(signature) {
        const parenthetical = mapFirefoxAppVersionParenthetical(signature);
        const mozVersion = parseMozillaVersion();
        if (mozVersion && parenthetical) {
            return mozVersion + " (" + parenthetical + ")";
        }
        return "";
    }

    function constructAppVersion(signature) {
        const browserName = signature.browser.name;
        switch (browserName) {
            case 'Chromium':
            case 'Chrome':
                return userAgent.substring('Mozilla/'.length);
            case 'Firefox':
                return constructFirefoxAppVersion(signature);
            default:
                return '';
        }
    }

    this.navigator = function () {
        return navigatorProjection;
    };

}

