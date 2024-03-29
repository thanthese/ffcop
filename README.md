# ffcop

Free and Friendly COP.

## Requirements

- Java 6
- [Leiningen](https://github.com/technomancy/leiningen)
- Postgresql 8.4, with postgis
- [Geoserver](http://geoserver.org/display/GEOS/Welcome)

## Usage

The first time, install dependencies

    lein deps

and initialize the database

    sh src/ffcop/database/reinit-db.sh

and you'll also need to setup your geoserver to match the config
settings in `src/ffcop/config.clj`.

### Run from REPL

    lein repl
    (-main)

### Run from terminal

    lein ring server

### Build war

Option 1: with context root

    lein ring uberwar ffcop.war

Depending on how the location of your tomcat and ffcop directory, you
may be able to simply run `sh super_deploy_dev.sh`.

Option 2: without context root

    lein ring uberwar ROOT.war

## A note to developers

Conventions are listed in the `dev-conventions.md` file.

## License

Copyright (C) 2011 thanthese productions

Distributed under the Eclipse Public License, the same as Clojure.
