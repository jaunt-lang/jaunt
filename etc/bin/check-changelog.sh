#!/bin/bash

BRANCH=$(git name-rev --name-only HEAD)

case $BRANCH in
  master|develop|release*)
    exit 0
    ;;
  
  *)
    if ! git diff --name-only origin/develop HEAD | grep -ie changelog.md > /dev/null
    then
      echo "No changelog entry in this patch!"
      exit 1
    fi
    ;;
esac
