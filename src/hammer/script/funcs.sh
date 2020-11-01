#!/bin/bash

# functions to run hammer test

extractModules() {
    if [ ! -d $moduleDir ]; then
        mkdir -p $moduleDir
        if [ -e $releaseZip ]; then
            unzip -qo $releaseZip -d $workDir
            mv $workDir/scoopi-$scoopiVersion/module $workDir
            rm -rf $workDir/scoopi-$scoopiVersion
        else
            echo "$releaseZip not found, build project first"
        fi
    fi
}

cleanup() {
    sudo rm -rf $scoopiOutputDir
    sudo rm -rf $runDir
}

runScoopi() {
    pids=()
    for node in ${nodes[@]}; do

        NODE_OPTS="-Dscoopi.log.dir=$logsDir/$node"        

        JAVA_OPTS+="-Dscoopi.project=hammer "
        JAVA_OPTS+="-Dscoopi.appender.file.baseDir=/tmp/hammer/run "
        JAVA_OPTS+="-Dscoopi.cluster.log.path.suffixUid=false "
        JAVA_OPTS+="-Dhazelcast.config=/hazelcast-tcp.xml "

        java $JAVA_OPTS $NODE_OPTS -cp $moduleDir/*:conf:. org.codetab.scoopi.Scoopi >/dev/null &
        pids+=($!)
    done

    echo ${pids[@]}
}

runScoopiInDocker() {
    pids=()

    JAVA_OPTS+="-Dscoopi.project=hammer "
    JAVA_OPTS+="-Dhazelcast.config=/hazelcast-mcast.xml "
    export JAVA_OPTS

    docker-compose --project-directory . -f docker/docker-compose.yaml up -d
    sleep 1
    pids=($(pgrep -f scoopi.project=hammer ))

    echo ${pids[@]}
}

isPidAlive() {
    if ps -p $1 >/dev/null; then
        echo "true"
    else
        echo "false"
    fi
}

waitForFinish() {
    count=0
    while true; do
        anyAlive="false"
        for pid in "$@"; do
            if [[ "$(isPidAlive $pid)" == "true" ]]; then
                anyAlive="true"
            fi
        done
        if [[ "$anyAlive" == "true" ]]; then
            sleep 0.5
            let count=count+1
            if ((count % 4 == 0)); then
                echo -n "."
            fi
        else
            sleep 0.5
            break
        fi
    done
}

scheduleNodeCrash() {

    count=0
    slept=0
    while true; do
        if (($(echo $slept == $crashAfter | bc -l))); then
            if [[ "$(isPidAlive ${nodesToCrash[0]})" == "true" || "$(isPidAlive ${nodesToCrash[1]})" == "true" ]]; then
                sudo kill -$killSignal ${nodesToCrash[@]}
                echo -n " pid ${nodesToCrash[@]} killed at $slept"
            else
                echo -n " pid ${nodesToCrash[@]} completed, can't kill"
            fi
            echo -n " "
            break
        else
            sleep $incrementBy
            slept=$(echo "$slept + $incrementBy" | bc -l)

            count=$(($count + 1))

            if [[ $(echo "$slept % 2" | bc) == 0 ]]; then
                echo -n "."
            fi
        fi
    done
}

checkConsistency() {
    errorDir="$workDir/errors/$runName/$crashAfter"
    rm -rf $errorDir

    sudo chown m.m $outputDir -R
    # prepare actual file
    sort -t '|' -k 1 -k 2 -k 3 -k 4 $outputDir/$dirTimestamp/data-*.txt >$outputDir/$dirTimestamp/actual.txt

    # prepare expected file
    cp $defsDir/expected.txt $outputDir/$dirTimestamp/expected.tmp
    sed -i "s/%{runDateTime}/$runDateTime/" $outputDir/$dirTimestamp/expected.tmp
    sed -i "s/%{documentFromDate}/$documentFromDate/" $outputDir/$dirTimestamp/expected.tmp
    sort -t '|' -k 1 -k 2 -k 3 -k 4 $outputDir/$dirTimestamp/expected.tmp >$outputDir/$dirTimestamp/expected.txt

    diff -q $outputDir/$dirTimestamp/expected.txt $outputDir/$dirTimestamp/actual.txt > /dev/null
    if [[ $? == 0 ]]; then
        echo "  $(tput setaf 2)success$(tput sgr0)"
    else
        echo "  $(tput setaf 1)failure$(tput sgr0)"
        mkdir -p $errorDir
        cp -r $outputDir/$dirTimestamp/* $errorDir
        cp -r $logsDir/* $errorDir
    fi
}
