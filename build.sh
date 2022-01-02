set -e
export SCRIPT_DIR=$(cd -P $(dirname "$0") && pwd -P)
cd $SCRIPT_DIR
rm -fR org target
mvn package
7z x target/antlrworks-$version-complete.jar org/antlr/codegen/templates/Java/ASTParser.stg
patch org/antlr/codegen/templates/Java/ASTParser.stg patch/org/antlr/codegen/templates/Java/ASTParser.patch
7z a target/antlrworks-$version-complete.jar org/antlr/codegen/templates/Java/ASTParser.stg

