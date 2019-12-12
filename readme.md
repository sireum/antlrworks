This is a fork of [antlr/antlrworks](https://github.com/antlr/antlrworks) with the following changes:

* Upgraded to Antlr 3.5.2 and Java 8
* Fixed various exception
* Removal of OS X-specific and ant artifacts
* Removal of feedback submission and update checking

To build:

```bash
version=VERSION mvn package  # default version is SNAPSHOT
``` 

To run:

```bash
java -jar target/antlrworks-VERSION-complete.jar
```
