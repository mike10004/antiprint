document.addEventListener("click", evt => {

    if (evt.target.id === 'settings-link') {
        browser.tabs.create({
            active: true,
            url: browser.extension.getURL('browser-settings.html')
        });
    }
});
