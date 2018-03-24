moment.locale('pl');
moment.tz.setDefault("Europe/Warsaw");
console.log("Setting moment");

function quickPreset(preset) {
    console.log("preset: " + preset)
    $('.select-image').removeClass('select-image-selected');
    $('.select-image[selectvalue=' + preset + ']').addClass('select-image-selected');
    displayCurrentGallery();
}