(function(){
  const s = document.createElement('script');
  // TODO: add "script.js" to web_accessible_resources in manifest.json
  s.src = chrome.extension.getURL('platform-reform.js');
  s.onload = function() {
      this.remove();
  };
  const docHead = document.head || document.documentElement;
  docHead.appendChild(s);
})();
