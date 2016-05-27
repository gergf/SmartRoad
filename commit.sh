#!/bin/bash -e 
mes="$1"
git add src/*
git add docs/* 
git add README.md
git add pom.xml 
git commit -m "$mes"