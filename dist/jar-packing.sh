#!/bin/sh

# combine jar-loader script and jar into a self-contained excutable script.

cat jar-loader.sh csvx.jar > csvx && chmod +x csvx 
