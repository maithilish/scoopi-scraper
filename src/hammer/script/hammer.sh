#!/bin/bash

# run cli scoopi cluster

# options - defaults
runName="run-a"
crashStartOffset=1
incrementBy=1
numOfTimes=25
parser=jsoupDefault

# constants
scoopiVersion=0.9.8-beta
runDateTime="30-01-2020T10:20:05"
documentFromDate="30-01-2020T10:20:05"
dirTimestamp=2020Jan30-102005
workDir=/tmp/hammer
runDir=$workDir/run
scoopiOutputDir=output
outputDir=$runDir/output
logsDir=$runDir/logs
projectBase=/orange/data/eclipse-workspace/scoopi-scraper
releaseZip=$projectBase/scoopi/target/scoopi-$scoopiVersion-release.zip
moduleDir=$workDir/module

source ./script/opts.sh
source ./script/funcs.sh

# export to docker
export logsDir
export outputDir

## configs

JAVA_OPTS="-Dscoopi.cluster.quorum.size=2 "
JAVA_OPTS+="-Dlog4j.configurationFile=conf/log4j2.xml "
JAVA_OPTS+="-Dscoopi.metrics.server.enable=false "
JAVA_OPTS+="-Dscoopi.defs.dir=$defsDir "
JAVA_OPTS+="-Dscoopi.defs.defaultSteps=$parser "

## main

extractModules

for ((c = 0; c < numOfTimes; c++)); do

    cleanup

    nodes=("node-a" "node-b" "node-c" "node-d")

    if [[ "$runtime" == "java" ]]; then
      pids=($(runScoopi))
    else
      pids=($(runScoopiInDocker))    
    fi
   
    crashAfter=$(echo "$crashStartOffset + ($c * $incrementBy)" | bc)

    echo -n "[$runName] #$(($c + 1)), kill after: $crashAfter seconds "

    if [[ "$killHowMany" == "1" ]]; then
      nodesToCrash=(${pids[0]})
    else
      nodesToCrash=(${pids[0]} ${pids[2]})
    fi

    scheduleNodeCrash

    waitForFinish ${pids[@]}

    checkConsistency

done