//radio and checkbox selection, active class toggle
require(["jquery"],function($) {

  var label = $('label.form-radio.selectable, label.form-checkbox.selectable');
  
  label.each(function() {
    var input = $(this).children('input:radio, input:checkbox');
    input.on('change', function() {
      if(input.is(':checked')) {
        $('label').removeClass('selected');
        $(this).parent('label').addClass('selected');
        input.parent(label).addClass('selected');
      } 
      if(!input.is(':checked')) {
        input.parent(label).removeClass('selected');
      }
    });
  });

});