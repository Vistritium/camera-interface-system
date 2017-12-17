moment.locale('pl');

function quickPreset(preset) {
    console.log("preset: " + preset)
    $('.select-image').removeClass('select-image-selected');
    $('.select-image[selectvalue=' + preset + ']').addClass('select-image-selected');
    displayCurrentGallery();
}