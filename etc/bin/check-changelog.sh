#!/bin/bash

if ! git diff --name-only origin/develop HEAD | grep -ie changelog.md > /dev/null
then
  echo "No changelog entry in this patch!"
  exit 1
fi
