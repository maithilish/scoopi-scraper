#!/bin/bash

# run cli scoopi cluster

# options - defaults
runName="run-a"
crashBeginAt=1
incrementBy=1
numOfRuns=25
parser=jsoup
numOfKills=1
killSignal=15

# constants
scoopiVersion=1.0.0
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

nodes=($(getNodesList $numOfServers $numOfClients))

for ((c = 0; c < numOfRuns; c++)); do

  cleanup

  if [[ "$engine" == "java" ]]; then
    pids=($(runScoopi))
  else
    pids=($(runScoopiInDocker))
  fi

  crashAfter=$(echo "$crashBeginAt + ($c * $incrementBy)" | bc)

  echo -n "[$runName] #$(($c + 1)) "

  nodesToCrash=()
  for ((i = 0; i < ${#killIndexes[@]}; i++)); do
    index=${killIndexes[$i]}
    nodesToCrash+=(${pids[$index]})
  done

  scheduleNodeCrash

  waitForFinish ${pids[@]}

  checkConsistency

done
