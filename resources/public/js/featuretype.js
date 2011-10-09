var ft = function() {

  function sanitize(text) {
    return text.replace(/\|/g, "")
  }

  return {
    addField: function() {
      $('tr.field:last').clone().appendTo('table.fields')
      $('tr.field:last .name').val("")
    },

    deleteField: function(span) {
      if( $('tr.field').length > 1 ) {
        $(span).parents('tr.field').remove()
      }
    },

    onsubmit: function() {
      var serialized = $('tr').map(function(i, tr) {
          return sanitize($(tr).find('input').val())
               + "|"
               + sanitize($(tr).find('select :selected').text())})
        .toArray().join("|")
      $("#featuretype-fields").val(serialized)
    }
  }
}()

