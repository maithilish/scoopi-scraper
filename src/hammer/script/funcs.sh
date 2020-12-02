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
    for ((c = 0; c < ${#nodes[@]}; c++)); do

        NODE_OPTS="-Dscoopi.log.dir=$logsDir/node-$c"

        L_JAVA_OPTS=$JAVA_OPTS
        L_JAVA_OPTS+="-Dscoopi.project=hammer "
        L_JAVA_OPTS+="-Dscoopi.appender.file.baseDir=/tmp/hammer/run "
        L_JAVA_OPTS+="-Dscoopi.cluster.log.path.suffixUid=false "

        if [[ ${nodes[$c]} == "server" ]]; then
            L_JAVA_OPTS+="-Dscoopi.cluster.config.file=/hazelcast-tcp.xml "
        else
            L_JAVA_OPTS+="-Dscoopi.cluster.mode=client "
            L_JAVA_OPTS+="-Dscoopi.cluster.config.file=/hazelcast-client.xml "
        fi

        java $L_JAVA_OPTS $NODE_OPTS -cp $moduleDir/*:conf:. org.codetab.scoopi.Scoopi >/dev/null &

    done
    pids=($(pgrep -f scoopi.project=hammer))
    echo "${pids[@]}"
}

runScoopiInDocker() {
    pids=()
    for ((c = 0; c < ${#nodes[@]}; c++)); do

        L_JAVA_OPTS=$JAVA_OPTS
        L_JAVA_OPTS+="-Dscoopi.project=hammer "

        if [[ ${nodes[$c]} == "server" ]]; then
            L_JAVA_OPTS+="-Dscoopi.cluster.config.file=/hazelcast-multicast.xml "
        else
            L_JAVA_OPTS+="-Dscoopi.cluster.mode=client "
            L_JAVA_OPTS+="-Dscoopi.cluster.config.file=/hazelcast-client.xml "
        fi

        cName="hammer-node-$c"

        docker rm $cName >/dev/null 2>&1

        docker run --name $cName -d \
            -v $PWD/conf:/scoopi/conf \
            -v $PWD/defs:/scoopi/defs \
            -v $logsDir:/scoopi/logs \
            -v $outputDir:/scoopi/output \
            -e JAVA_OPTS="$L_JAVA_OPTS" \
            codetab/scoopi:$scoopiVersion >/dev/null 2>&1
        #pids+=($!)
    done
    sleep 1
    pids=($(pgrep -f "java.*scoopi.project=hammer"))
    echo "${pids[@]}"
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
                echo -n " [pid ${nodesToCrash[@]} killed at ${crashAfter}s]"
            else
                echo -n " pid ${nodesToCrash[@]} finished, can't kill"
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

    diff -q $outputDir/$dirTimestamp/expected.txt $outputDir/$dirTimestamp/actual.txt >/dev/null
    if [[ $? == 0 ]]; then
        echo "  $(tput setaf 2)success$(tput sgr0)"
    else
        echo "  $(tput setaf 1)failure$(tput sgr0)"
        mkdir -p $errorDir
        cp -r $outputDir/$dirTimestamp/* $errorDir
        cp -r $logsDir/* $errorDir
    fi
}

getNodesList() {
    servers=$1
    clients=$2

    nodes=()
    for ((c = 0; c < $servers; c++)); do
        nodes+=("server")
    done
    for ((c = 0; c < $clients; c++)); do
        nodes+=("client")
    done
    echo ${nodes[@]}
}
