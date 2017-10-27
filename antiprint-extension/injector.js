(function(){

  const scriptEl = document.createElement('script');
  scriptEl.src = chrome.extension.getURL('platform-reform.js');
  scriptEl.onload = function() {
      this.remove();
  };
  const docHead = document.head || document.documentElement;
  docHead.appendChild(scriptEl);
})();
