require.config({
    paths: {
        'jquery': 'lib/jquery/jquery-1.9.1.min',
        'jquery-migrate': 'lib/jquery/jquery-migrate-1.2.1.min',
        'header-footer-only': 'header-footer-only',
        'form-checked-selection': 'form-checked-selection',
        'page-init': '../lib/vehicles-presentation-common/javascripts/page-init',
        'global-helpers': '../lib/vehicles-presentation-common/javascripts/global-helpers'
    }
});

require(
    ["jquery", "jquery-migrate", "header-footer-only", "form-checked-selection", "page-init", "global-helpers"],
    function($, jqueryMigrate, headerFooterOnly, formCheckedSelection, pageInit) {
        pageInit.initAll()
    }
);
