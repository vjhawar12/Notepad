#!/bin/bash

cd "src" || exit
find . -name '*.class' -exec rm -rf {} \; 
javac Main.java
java Main	