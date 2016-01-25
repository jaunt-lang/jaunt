#!/bin/sh

astyle \
    --style=java\
    --indent=spaces=2\
    --pad-header\
    --add-brackets\
    --convert-tabs\
    --suffix=none\
    --recursive\
    src/jvm/*.java
    
