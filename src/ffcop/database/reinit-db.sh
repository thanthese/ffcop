#!/bin/sh

dropdb -U postgres featuretype
dropuser -U postgres ffcop

createuser -U postgres --superuser ffcop
createdb -U ffcop featuretype
psql -U ffcop featuretype -c "alter user ffcop password 'magic-unlock'"

createlang plpgsql -U postgres featuretype
psql -U postgres -f /usr/share/postgresql/8.4/contrib/postgis-1.5/postgis.sql -d featuretype

psql -U ffcop featuretype -c "create table example1 (id integer)"
psql -U ffcop featuretype -c "create table example2 (id integer)"

psql -U ffcop featuretype -c "insert into example1 values (1)"
psql -U ffcop featuretype -c "insert into example1 values (2)"
psql -U ffcop featuretype -c "insert into example1 values (3)"
