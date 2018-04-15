function displayGallery(images) {

    var pswpElement = document.querySelectorAll('.pswp')[0];

    var width = 1280;
    var height = 720;

    var items = _.map(images, function (image) {
        return {
            src: getImageSrc(image),
            title: getImageText(image),
            fullpath: image.fullpath,
            id: image.id,
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

    gallery.listen('shareLinkClick', function (e, target) {
        console.log(target)
    });

    window.gallery = gallery

}

function getImageSrc(image) {
    return "/images/download/" + image.fullpath;
}

function getImageText(image) {
    return moment(image.phototaken).format('LLLL');
}

$(document).ready(function () {
    $('.pswp__button--favourite').click(function () {
        console.log(window.gallery.currItem);
        var fullPath = window.gallery.currItem.fullpath;
        console.log("fullPath: " + fullPath)

        $.ajax({
            url: "/api/googledrive/upload/" + encodeURIComponent(fullPath),
            method: 'POST'
        }).done(function (data, status) {
            console.log("success")
            console.log(data);
            $('#favourite-success-modal').modal('show')
        }).fail(function (data, status) {
            console.log("failure")
            console.log(data);
            console.log(status);
            $('#favourite-failure-modal').modal('show')
        })


    })
});