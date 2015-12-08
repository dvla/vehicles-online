// Define the dependency to page-init in common
define(['jquery', 'jquery-migrate', "page-init"], function($, jqueryMigrate, pageInit) {

    var addGaEvents = function() {
        var gaTodaysDateClickedClass = "ga-use-todays-date-clicked"
        $('#todays_date').on('click', function() {
            if (!$(this).hasClass(gaTodaysDateClickedClass)) {
                _gaq.push(['_trackEvent', 'ct_link', 'use-todays-date', 'click', 1]);
                $(this).addClass(gaTodaysDateClickedClass);
            }
        });
    };

    return {
        init: function() {
            addGaEvents();

            // Call initAll on the pageInit object to run all the common js in vehicles-presentation-common
            pageInit.initAll();
        }
    }
});
