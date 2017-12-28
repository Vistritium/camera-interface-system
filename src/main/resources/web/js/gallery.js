function displayGallery(images){

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
        index: 0,
        loop: true,
        preload: [6, 6],
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
