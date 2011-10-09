var ft = function() {
  return {
    addField: function() {
      $('tr.field:last').clone().appendTo('table.fields')
      $('tr.field:last .name').val("")
    },

    deleteField: function(span) {
      if( $('tr.field').length > 1 ) {
        $(span).parents('tr.field').remove()
      }
    }
  }
}()

