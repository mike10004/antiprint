const fixturesPath = 'resources/fixtures';
fixtures.setPath(fixturesPath);
beforeAll(done => fixtures.load(done));
const fullFixturesPath = '/base/' + fixturesPath + '/';
const fixtureFiles = Object.keys(window.__karma__.files).filter(function(file) {
      return file.indexOf(fullFixturesPath) === 0;
});
const fixtureNames = fixtureFiles.map(f => f.slice(fullFixturesPath.length));
window.platformReformSettings = {
  disabled: true
};

describe('parseUserAgentSignature', function(){
  fixtureNames.forEach(fixtureName => {
    it(fixtureName.slice(".json".length), function() {
      console.debug('fixtureName', fixtureName);
      const testCase = fixtures.get(fixtureName);
      const actual = new SignatureCrafter({'userAgent': testCase.userAgent}).navigator();
      expect(actual.platform).toEqual(testCase.platform);
    });
  });
});

/* global describe, it, expect, fixtures, beforeAll, beforeEach */