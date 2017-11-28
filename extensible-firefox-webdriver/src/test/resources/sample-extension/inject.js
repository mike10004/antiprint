document.addEventListener('DOMContentLoaded', function() {
    const div = document.createElement('div');
    div.id = 'injected';
    div.innerText = 'Hello';
    document.body.append(div);
});