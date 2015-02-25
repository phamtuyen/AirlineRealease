#!/bin/bash
NAME="webapp"
DESC="Webapp service"

home=`dirname "${BASH_SOURCE-$0}"`
home=`cd "$home">/dev/null; pwd`

classpath=$home:$home/lib/*:$home/bin/*:$home/config/*

java -cp $classpath -Dlog4j.configurationFile=log4j.properties com.mbv.ticketsystem.launcher.Jetty
