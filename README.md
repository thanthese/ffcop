# ffcop

Free and Friendly COP.

## Requirements

- Java 6
- [leiningen](https://github.com/technomancy/leiningen)
- postgresql 8.4, with postgis

## Usage

The first time, install dependencies

    lein deps

and initialize the database

    sh src/ffcop/database/reinit-db.sh

### Run from REPL

    lein repl
    (-main)

### Run from terminal

    lein ring server

### Build war

    lein ring uberwar ROOT.war

Note: it looks like you *have* to deploy the app to tomcat as root. If
you don't, the addition of the app name in the url breaks all paths.

This trick plays nicely with a geoserver running in the same tomcat.

## License

Copyright (C) 2011 thanthese productions

Distributed under the Eclipse Public License, the same as Clojure.
