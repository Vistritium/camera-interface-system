$(document).ready(function () {

    $('.select-image').click(function (event) {
        $(event.target).toggleClass('select-image-selected');
        update();
    });

    $('#btn-selector-all').click(function (event) {
        $('.select-image').addClass('select-image-selected');
        update();
    });

    $('#btn-selector-none').click(function (event) {
        $('.select-image').removeClass('select-image-selected');
        update();
    });

    var cssOptions = {
        size: "large",
        picker: {
            "border-color": "#626d71"
        },
        element: {
            "font-weight": "bold",
            "color": "#654732"
        },
        selected: {
            "color": "#652D2F",
            "font-weight": "900"/*,
            "border-width": "5px"*/
        }
    };
    var multiPickerInitialized = false;
    var onSelectDeselect = function (a, b) {
        if (multiPickerInitialized) {
            update();
        }
    };

    var hours1Multipicker = $("#hours1").multiPicker({
        selector: "li",
        cssOptions: cssOptions,
        onSelect: onSelectDeselect,
        onUnselect: onSelectDeselect
    });
    var hours2Multipicker = $("#hours2").multiPicker({
        selector: "li",
        cssOptions: cssOptions,
        onSelect: onSelectDeselect,
        onUnselect: onSelectDeselect
    });

    var hour1Size = $("#hours1 > li").length;
    var hour2Size = $("#hours2 > li").length;

    hours1Multipicker.multiPicker('select', _.range(hour1Size));

    $('#all-hours').click(function () {
        hours1Multipicker.multiPicker('select', _.range(hour1Size));
        hours2Multipicker.multiPicker('select', _.range(hour2Size));
    });

    $('#none-hours').click(function () {
        hours1Multipicker.multiPicker('unselect', _.range(hour1Size));
        hours2Multipicker.multiPicker('unselect', _.range(hour2Size));
    });
    multiPickerInitialized = true;

    $('#granulation').change(function () {
        update()
    });

    var defaultFromDate = moment(window.maxDate).add(-1,"days").toDate();
    window.dateRangeSlider = $('#date-slider').dateRangeSlider({
        bounds: {min: window.minDate, max: window.maxDate},
        defaultValues: {min: defaultFromDate, max: window.maxDate}
    });



    $('#image-counter-text').click(function () {
        displayCurrentGallery();
    });

    var minValueForDateInput = moment(window.minDate).format("YYYY-MM-DD");
    var maxValueForDateInput = moment(window.maxDate).format("YYYY-MM-DD");
    var datePickerFrom = $('#date-picker-from');
    var datePickerTo = $('#date-picker-to');
    datePickerFrom[0].value = moment(defaultFromDate).format("YYYY-MM-DD");
    datePickerFrom.attr("min", minValueForDateInput);
    datePickerFrom.attr("max", maxValueForDateInput);
    datePickerTo[0].value = maxValueForDateInput;
    datePickerTo.attr("min", minValueForDateInput);
    datePickerTo.attr("max", maxValueForDateInput);

    datePickerFrom.change(function(){
        var dateValues = window.dateRangeSlider.dateRangeSlider("values");
        var value = datePickerFrom.val();
        window.dateRangeSlider.dateRangeSlider("values", new Date(value), dateValues.max)
    });

    datePickerTo.change(function(){
        var dateValues = window.dateRangeSlider.dateRangeSlider("values");
        var value = datePickerTo.val();
        window.dateRangeSlider.dateRangeSlider("values", dateValues.min, new Date(value))
    });

    window.dateRangeSlider.bind('userValuesChanged', function (x, y) {
        update();
        var dates = getDates();
        console.log("values changing: " + dates);
        datePickerFrom[0].value = moment(dates.min).format("YYYY-MM-DD");
        datePickerTo[0].value = moment(dates.max).format("YYYY-MM-DD");
    });

});

function getSelectedHours() {
    var res = [];
    $('#hours1 > .active').each(function (i, elem) {
        res.push($(elem).text().trim());
    });
    $('#hours2 > .active').each(function (i, elem) {
        res.push($(elem).text().trim());
    });
    return _.map(res, function (elem) {
        return Number(elem);
    });
}

function getGranulation() {
    return Number($('#granulation')[0].value);
}

function getSelectedPresets() {
    var values = [];
    $('.select-image-selected').each(function (i, elem) {
        values.push($(elem).attr('selectvalue'))
    });
    return values;
}

function updateCount(newCount) {
    $('#image-counter-value').text(newCount.toString())
}

function getDates() {
    return window.dateRangeSlider.dateRangeSlider("values");
}


function update() {
    getSearchData(true, function (data) {
        updateCount(data);
        $('#image-counter').css('display', 'initial');
    });
}
