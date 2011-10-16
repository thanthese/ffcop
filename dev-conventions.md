# Partial templates

- "h-" prefix.
- Do *not* access the database. Ideally the only functions they call, if
any, would be other partial templates.

# Actions/Views

Actions and views are combined into the same function. All database
activity and other "model" work happens in a `let` statement at the top.

# Database

All sql queries exist in `db.sql`. Queries that changes database state
have a `!` postfix.

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

# Coding conventions

- Functions do not contain blank lines.
