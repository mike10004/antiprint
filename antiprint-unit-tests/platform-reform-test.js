window.platformReformSettings = {
  disabled: true
};

describe('parseUserAgentSignature', function(){

  class TestCase {
    constructor(name, nav) {
      this.name = name;
      this.nav = nav;
    }
  }

  const testCases = [
    new TestCase('Chrome 60 on Mac OS 10.12.5', {
      "vendorSub":"",
      "productSub":"20030107",
      "vendor":"Google Inc.",
      "maxTouchPoints":0,
      "hardwareConcurrency":8,
      "cookieEnabled":true,
      "appCodeName":"Mozilla",
      "appName":"Netscape",
      "appVersion":"5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36",
      "platform":"MacIntel",
      "product":"Gecko",
      "userAgent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36",
      "language":"en-US",
      "onLine":true
    }),
    new TestCase("Safari on Mac OS 10.12.5", {
      "cookieEnabled":true,
      "hardwareConcurrency":8,
      "appCodeName":"Mozilla",
      "appName":"Netscape",
      "appVersion":"5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4",
      "platform":"MacIntel",
      "product":"Gecko",
      "productSub":"20030107",
      "userAgent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4",
      "vendor":"Apple Computer, Inc.",
      "vendorSub":"",
      "language":"en-US",
      "onLine":true
    }),
    new TestCase("Firefox 49 on Mac OS 10.12.5", {
      "doNotTrack":"unspecified",
      "oscpu":"Intel Mac OS X 10.12",
      "vendor":"",
      "vendorSub":"",
      "productSub":"20100101",
      "cookieEnabled":true,
      "buildID":"20161019084923",
      "hardwareConcurrency":8,
      "appCodeName":"Mozilla",
      "appName":"Netscape",
      "appVersion":"5.0 (Macintosh)",
      "platform":"MacIntel",
      "userAgent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:49.0) Gecko/20100101 Firefox/49.0",
      "product":"Gecko",
      "language":"en-US",
      "onLine":true
    }),
    new TestCase("Chrome 61 on Windows", {
      "vendorSub": "",
      "productSub": "20030107",
      "vendor": "Google Inc.",
      "maxTouchPoints": 0,
      "hardwareConcurrency": 4,
      "cookieEnabled": true,
      "appCodeName": "Mozilla",
      "appName": "Netscape",
      "appVersion": "5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
      "platform": "Win32",
      "product": "Gecko",
      "userAgent": "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
      "language": "en-US",
      "onLine": true
    }),
    new TestCase("Microsoft Internet Explorer 11 on Windows", {
      "appCodeName":"Mozilla",
      "appMinorVersion":"0",
      "browserLanguage":"en-US",
      "cookieEnabled":true,
      "cpuClass":"x86",
      "language":"en-US",
      "systemLanguage":"en-US",
      "userLanguage":"en-US",
      "maxTouchPoints":0,
      "msManipulationViewsEnabled":true,
      "msMaxTouchPoints":0,
      "msPointerEnabled":true,
      "pointerEnabled":true,
      "webdriver":false,
      "appName":"Netscape",
      "appVersion":"5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; rv:11.0) like Gecko",
      "platform":"Win32",
      "product":"Gecko",
      "userAgent":"Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; rv:11.0) like Gecko",
      "vendor":"",
      "onLine":true
    }),
    new TestCase("Firefox 47 on Windows", {
      "doNotTrack": "unspecified",
      "oscpu": "Windows NT 6.3; WOW64",
      "vendor": "",
      "vendorSub": "",
      "productSub": "20100101",
      "cookieEnabled": true,
      "buildID": "20161031133903",
      "appCodeName": "Mozilla",
      "appName": "Netscape",
      "appVersion": "5.0 (Windows)",
      "platform": "Win32",
      "userAgent": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0",
      "product": "Gecko",
      "language": "en-US",
      "onLine": true
    }),
    new TestCase("Firefox 56 on Ubuntu 17.04", {
      "doNotTrack": "unspecified",
      "oscpu": "Linux x86_64",
      "vendor": "",
      "vendorSub": "",
      "productSub": "20100101",
      "cookieEnabled": true,
      "buildID": "20171003100843",
      "hardwareConcurrency": 8,
      "appCodeName": "Mozilla",
      "appName": "Netscape",
      "appVersion": "5.0 (X11)",
      "platform": "Linux x86_64",
      "userAgent": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:56.0) Gecko/20100101 Firefox/56.0",
      "product": "Gecko",
      "language": "en-US",
      "onLine": true
    }),
    new TestCase("Chromium 61.0.3163.100 (Official Build) Built on Ubuntu , running on Ubuntu 17.04 (64-bit)", {
      "vendorSub": "",
      "productSub": "20030107",
      "vendor": "Google Inc.",
      "maxTouchPoints": 0,
      "hardwareConcurrency": 8,
      "cookieEnabled": true,
      "appCodeName": "Mozilla",
      "appName": "Netscape",
      "appVersion": "5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/61.0.3163.100 Chrome/61.0.3163.100 Safari/537.36",
      "platform": "Linux x86_64",
      "product": "Gecko",
      "userAgent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/61.0.3163.100 Chrome/61.0.3163.100 Safari/537.36",
      "language": "en-US",
      "onLine": true
    }),
    new TestCase("Chrome 61.0.3163.100 (Official Build) (64-bit)", {
      "vendorSub": "",
      "productSub": "20030107",
      "vendor": "Google Inc.",
      "maxTouchPoints": 0,
      "hardwareConcurrency": 8,
      "cookieEnabled": true,
      "appCodeName": "Mozilla",
      "appName": "Netscape",
      "appVersion": "5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
      "platform": "Linux x86_64",
      "product": "Gecko",
      "userAgent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
      "language": "en-US",
      "onLine": true
    })
  ];

  testCases.forEach(testCase => {
    it(testCase.name, function() {
      const actual = new PlatformReformer().parseUserAgentSignature(testCase.nav.userAgent);
      expect(actual.platform).toEqual(testCase.nav.platform);
    });
  });
});
