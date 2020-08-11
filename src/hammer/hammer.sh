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
runDateTime="Thu Jan 30 10:20:05 IST 2020"
documentFromDate="Thu Jan 30 10:20:05 IST 2020"
dirTimestamp=30Jan2020-045005
workDir=/tmp/hammer
runDir=$workDir/run
scoopiOutputDir=output
outputDir=$runDir/output
logsDir=$runDir/logs
releaseZip=../../scoopi/target/scoopi-$scoopiVersion-release.zip
moduleDir=$workDir/module

source ./opts.sh
source ./funcs.sh

## configs

JAVA_OPTS="-Dhazelcast.config=/hazelcast.xml "
JAVA_OPTS+="-Dscoopi.cluster.quorum.size=2 "
JAVA_OPTS+="-Dlog4j.configurationFile=conf/log4j2.xml "
JAVA_OPTS+="-Dscoopi.metrics.server.enable=false "
JAVA_OPTS+="-Dscoopi.defs.dir=$defsDir "
JAVA_OPTS+="-Dscoopi.defs.defaultSteps=$parser"

## main

extractModules

for ((c = 0; c < numOfTimes; c++)); do

    cleanup

    nodes=("node-a" "node-b" "node-c")

    pids=($(runScoopi))

    crashAfter=$(echo "$crashStartOffset + ($c * $incrementBy)" | bc)

    echo -n "[$runName] #$(($c + 1)), kill after: $crashAfter seconds "

    nodeToCrash=${pids[0]}
    scheduleNodeCrash

    waitForFinish ${pids[@]}

    checkConsistency

done
