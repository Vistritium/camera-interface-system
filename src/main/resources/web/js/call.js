function getSeatchDataRaw(min, max, granulation, selectedHours, selectedPresets, count, onSuccess) {
    var params = {
        min: min.toISOString(),
        max: max.toISOString(),
        granulation: granulation,
        hours: selectedHours.join(','),
        presets: selectedPresets.join(','),
        count: count
    };


    $.ajax({
        url: "/api/images",
        data: params,
        method: 'GET',
        success: onSuccess
    })
}

function getSearchData(count, onSuccess) {
    var granulation = getGranulation();
    var selectedHours = getSelectedHours();
    var selectedPresets = getSelectedPresets();
    var dates = getDates();
    var min = dates.min;
    var max = dates.max;
    getSeatchDataRaw(min, max, granulation, selectedHours, selectedPresets, count, onSuccess);
}

function displayCurrentGallery(){
    getSearchData(false, function (data) {
        displayGallery(data)
    });
}