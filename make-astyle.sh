#!/bin/sh

if [ ! -f ~/bin/astyle ]; then
    curl -L http://downloads.sourceforge.net/project/astyle/astyle/astyle%202.05.1/astyle_2.05.1_linux.tar.gz\
        -o astyle.tar.gz
    tar -xzf astyle.tar.gz
    cd astyle/build/gcc; make
    sudo mv bin/astyle ~/bin/
fi
