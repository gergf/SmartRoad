#!/bin/bash -e 
mes="$1"
git add --all src/*
git add --all docs/* 
git add README.md
git add pom.xml 
git commit -m "$mes"