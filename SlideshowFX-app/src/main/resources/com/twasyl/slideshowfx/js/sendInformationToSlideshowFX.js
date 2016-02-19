function sendInformationToSlideshowFX(source) {
    dashIndex = source.id.indexOf("-");
    slideNumber = source.id.substring(0, dashIndex);
    fieldName = source.id.substring(dashIndex+1);

    sfx.prefillContentDefinition(slideNumber, fieldName, window.btoa(unescape(encodeURIComponent(source.innerHTML))));
}