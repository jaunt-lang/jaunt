#!/bin/bash

VERSION="$(cat VERSION)-test-SNAPSHOT"
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$VERSION
mvn install -Dmaven.test.skip=true

if [ ! -d "cider-nrepl" ]
then
  git clone git@github.com:clojure-emacs/cider-nrepl.git
else
  (cd cider-nrepl; git reset --hard origin/master; git pull)
fi

cd cider-nrepl &&
lein do clean, source-deps :project-prefix cider.inlined-deps &&
lein with-profile +plugin.mranderson/config,+test-clj,+test-cljs update-in :dependencies conj "[org.jaunt-lang/jaunt \"$VERSION\"]" -- test
