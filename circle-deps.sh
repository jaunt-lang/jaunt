#!/bin/bash

M2="$HOME/.m2"
FLAGS="$M2/tags"
mkdir -p "$FLAGS"
FILE="$FLAGS/$(cat pom.xml build.xml circle-deps.sh | grep -v "<version>.*</version>" | shasum -a 512 | awk '{print $1}')"
if [ ! -e "$FILE" ]
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

  # purge jaunt from m2
  # no reason to leave those lying about
   rm -r "$M2/repository/org/jaunt-lang/jaunt/"
  
  # leave the flag file behind
  touch "$FILE"
fi
