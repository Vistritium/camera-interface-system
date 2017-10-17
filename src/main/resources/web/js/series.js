$(document).ready(function () {

    if (window.images) {
        handleImageSeries(window.images);
    }


});


function handleImageSeries(images) {

    console.log("Images: " + images.length);

    var currentImage = images[0];
    var imageNameHandle = $('#image-info');
    var imageHandle = $('#image-panel-container .link-image');

    function selectImage(image) {
        currentImage = image;
        changeText(image);
        console.log("Selected image " + image.fullpath);
        imageHandle.attr("src", "/images/download/" + image.fullpath);
    }

    function changeText(image) {
        var formattedDate = moment(image.phototaken).format('LLLL');
        imageNameHandle.text(formattedDate);
    }


    $("#slider-container").slider({
        max: images.length - 1,
/*        stop: function (event, ui) {
            selectImage(images[ui.value])
        },*/
        slide: function (event, ui) {
            selectImage(images[ui.value])
        }
    });

    selectImage(images[0])
}

