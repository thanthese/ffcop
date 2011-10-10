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

    lein ring uberwar ffcop.war

## License

Copyright (C) 2011 thanthese productions

Distributed under the Eclipse Public License, the same as Clojure.
