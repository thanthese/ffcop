# Naming

Partial html templates have a "h-" prefix.

# REST

PUT, POST, and DELETE verbs happen on the same url as the GET.

# Redirect

*Renders*: use when "redirecting" to the same url (possibly with a
different verb).

*Redirects*: use when redirecting to a different url.

# Context root issues

Always use "/" to refer to the context root (css, js, renders, links,
forms, ...). The exception is redirects, where you *must* to relative
paths.
