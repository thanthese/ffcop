var ft = function() {

  function sanitize(text) {
    return text.replace(/\|/g, "")
  }

  return {
    addField: function() {
      $('tr.field:last').clone().appendTo('table.fields')
      $('tr.field:last .name').val("")
      $('tr.field:last input').focus()
    },

    deleteField: function(span) {
      if( $('tr.field').length > 1 ) {
        $(span).parents('tr.field').remove()
      }
    },

    onsubmit: function() {
      var serialized = $('tr.field').map(function(i, tr) {
          return sanitize($(tr).find('input').val())
               + "|"
               + sanitize($(tr).find('select :selected').text())})
        .toArray().join("|")
      $("#serialized-ft-fields").val(serialized)
    },

    ondelete: function() {
      return confirm("WARNING: This operation cannot be undone. "
          + "Are you sure you want to delete this featuretype?")
    }
  }
}()

$(function() {
  var textfield = $('input.text:first')
  if(textfield) textfield.focus().select()
})
