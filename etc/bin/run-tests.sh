#!/bin/bash

function do_tests () {
  case $CIRCLE_NODE_INDEX in
    0)
      ant test-example 2>&1 | tee test-example.log
      ;;
    1)
      ant test-generative 2>&1 | tee test-generative.log
      ;;
    2)
      bash etc/bin/test-cider.sh 2>&1 | tee test-cider.log
      ;;
  esac
}

mvn clean compile           # git info file, classfiles
bash etc/bin/antsetup.sh    # set up standalone classpath

if [ -n "$CIRCLE_NODE_INDEX" ]
then
  do_tests
else
  ( export CIRCLE_NODE_INDEX=0;
    do_tests > /dev/null ) &
  tex=$1

  ( export CIRCLE_NODE_INDEX=1;
    do_tests > /dev/null ) &
  teg=$1

  ( export CIRCLE_NODE_INDEX=2;
    do_tests > /dev/null ) &
  tec=$1
  
  wait $tex
  texr=$?
  wait $teg
  tegr=$?
  wait $tec
  tecr=$?

  if [ $texr ] && [ $tegr ] && [ $tecr ]
  then
    echo "Tests OK!"
  else
    [ ! $texr ] && echo "Example tests failed, see test-example.log"
    [ ! $tegr ] && echo "Generative tests failed, see test-generative.log"
    [ ! $tecr ] && echo "CIDER tests failed, see test-cider.log"
    echo "Tests failed :("
    exit 1
  fi
fi
