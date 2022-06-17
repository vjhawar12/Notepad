#!/bin/bash
#don't worry about this file
cd ..
cd src || exit
cd com || exit
find . -name '*.class' -exec rm -rf {} \;