#!/bin/bash
ROOT=$(git rev-parse --show-toplevel)
mvn -q dependency:build-classpath -Dmdep.outputFile=$ROOT/maven-classpath
cat <<EOF >$ROOT/maven-classpath.properties
maven.compile.classpath=`cat $ROOT/maven-classpath`
maven.test.classpath=`cat $ROOT/maven-classpath`
EOF
rm $ROOT/maven-classpath
echo "Wrote maven-classpath.properties for standalone ant use"
