define(['../lib/vehicles-presentation-common/javascripts/config'], function() {
    require.config({
        paths: {
            'jquery-migrate': '../lib/jquery-migrate/jquery-migrate'
        },
        // Make jquery-migrate depend on the loading of jquery
        "shim": {
            'jquery-migrate': ['jquery']
        }
    });
});
