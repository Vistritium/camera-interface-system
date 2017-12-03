$(document).ready(function () {

    if (window.images) {
        imageSeries(window.images);
    }

});


function imageSeries(images) {
    var pswpElement = document.querySelectorAll('.pswp')[0];

    var width = 1280;
    var height = 720;


    var items = _.map(images, function (image) {
        return {
            src: getImageSrc(image),
            title: getImageText(image),
            w: width,
            h: height
        }
    });

    var options = {
        // optionName: 'option value'
        // for example:
        index: images.length - 1, // start at first slide,
        loop: false,
        preload: [3, 3],
        shareEl: false
    };

// Initializes and opens PhotoSwipe
    var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
    gallery.init();
}

function getImageSrc(image) {
    return "/images/download/" + image.fullpath;
}

function getImageText(image){
    return moment(image.phototaken).format('LLLL');
}


function handleImageSeries(images) {

    console.log("Images: " + images.length);

    var currentImage = images[images.length - 1];
    var imageNameHandle = $('#image-info');
    var imageHandle = $('#image-panel-container .link-image');

    function selectImage(image) {
        currentImage = image;
        changeText(image);
        console.log("Selected image " + image.fullpath);
        imageHandle.attr("src",);
    }

    function changeText(image) {
        var formattedDate = moment(image.phototaken).format('LLLL');
        imageNameHandle.text(formattedDate);
    }


    $("#slider-container").slider({
        max: images.length - 1,
        value: images.length - 1,
        /*        stop: function (event, ui) {
                    selectImage(images[ui.value])
                },*/
        slide: function (event, ui) {
            selectImage(images[ui.value])
        }
    });

    selectImage(images[images.length - 1])
}

