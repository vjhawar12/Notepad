#!/bin/bash

cd "src"
find . -name '*.class' -exec rm -rf {} \; 
javac Main.java
java Main	