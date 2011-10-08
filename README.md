# ffcop

Free and Friendly COP.

## Requirements

- Java 6
- [leiningen](https://github.com/technomancy/leiningen).
- postgres 8.4 + postgis

## Usage

The first time, run this first:

    lein deps

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
