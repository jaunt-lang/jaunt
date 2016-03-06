#!/bin/sh

astyle --dry-run --options=astylerc src/jvm/*.java | grep Formatted && exit 1 || exit 0
