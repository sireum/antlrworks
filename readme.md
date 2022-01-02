This is a fork of [antlr/antlrworks](https://github.com/antlr/antlrworks) with the following changes:

* Upgraded to Antlr 3.5.2 with support for Java 8+
* Fixed various exceptions
* Removal of OS X-specific and ant artifacts
* Removal of update checking and submission of feedback, statistics, and registration
* Moved calls to TreeAdaptor.setTokenBoundaries before TreeAdaptor.rulePostProcessing in Java codegen template
  [[patch](patch/org/antlr/codegen/templates/Java/ASTParser.patch)]

To build:

```bash
version=VERSION ./build.sh  # default version is SNAPSHOT
``` 

To run:

```bash
java -jar target/antlrworks-VERSION-complete.jar
```
