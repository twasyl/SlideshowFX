function slideshowFXSetField(slide, what, value) {
    element = document.getElementById(slide + "-" + what);
    element.innerHTML = decodeURIComponent(escape(window.atob(value)));
}