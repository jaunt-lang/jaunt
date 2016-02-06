#!/bin/bash

DIR="~/.m2/tags"
FILE="$DIR/$(cat pom.xml build.xml circle-deps.sh | grep -v "<version>.*</version>" | shasum -a 512 | awk '{print $1}')"
mkdir -p "$DIR"
if [ ! -f "$FILE" ]
then
  # Do a deploy
  # - Forces a build
  # - Forces but does not run tests
  # - Uses all plugins
  # - Without a profile, the deploy fails
  mvn deploy -Dmaven.test.skip=true

  # use the versions plugin
  mvn versions:set -DgenerateBackupPoms=false -DnewVersion=whatever
  git checkout pom.xml

  # purge clojarr from m2
  # no reason to leave those lying about
  rm -r ~/.m2/repository/me/arrdem/clojarr/
  
  # leave the flag file behind
  touch "$FILE"
fi
