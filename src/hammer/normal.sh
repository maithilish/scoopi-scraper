#!/bin/bash

# Test Suite - hammer multiple example defs

baseDir=$PWD
workDir=/tmp/hammer
logFile=$workDir/hammer.log
errorLogFile=$workDir/error.log
defsBase=defs

trap ctrl_c INT

function ctrl_c() {
    alien='\U1F47D \U1F47D \U1F47D'
    echo -e "\n\n$alien   cancel requested: kill Scoopi processes and exit\n"
    sudo pkill -f Scoopi
    exit 1
}

hammer() {
    script/hammer.sh $@ 2>$errorLogFile | tee -a $logFile
}

sudo rm -rf $workDir
mkdir -p $workDir
touch $logFile

# kill 1
seq=1
# kill server, no client
hammer -n one-$((seq++)) -r 25 $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 $defsBase/examples/fin/jsoup/ex-13
hammer -n one-$((seq++)) -r 25 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 -e docker $defsBase/examples/fin/jsoup/ex-13

# kill server, with client
hammer -n one-$((seq++)) -r 25 -c 1 $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 -c 1 $defsBase/examples/fin/jsoup/ex-13
hammer -n one-$((seq++)) -r 25 -c 1 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 -c 1 -e docker $defsBase/examples/fin/jsoup/ex-13

# kill client
hammer -n one-$((seq++)) -r 25 -c 1 -k 3 $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 -c 1 -k 3 $defsBase/examples/fin/jsoup/ex-13
hammer -n one-$((seq++)) -r 25 -c 1 -k 3 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -n one-$((seq++)) -r 25 -c 1 -k 3 -e docker $defsBase/examples/fin/jsoup/ex-13

# kill 2
seq=1
# kill servers
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 $defsBase/examples/fin/jsoup/ex-1
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 $defsBase/examples/fin/jsoup/ex-13
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -e docker $defsBase/examples/fin/jsoup/ex-13

# kill servers, sigkill -s 9
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -s 9 $defsBase/examples/fin/jsoup/ex-1
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -s 9 $defsBase/examples/fin/jsoup/ex-13
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -s 9 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -n two-$((seq++)) -r 25 -m 5 -k 0,1 -s 9 -e docker $defsBase/examples/fin/jsoup/ex-13

# kill server and client
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -p htmlUnit $defsBase/examples/fin/htmlunit/ex-1
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -p htmlUnit $defsBase/examples/fin/htmlunit/ex-13
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -p htmlUnit -e docker $defsBase/examples/fin/htmlunit/ex-1
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -p htmlUnit -e docker $defsBase/examples/fin/htmlunit/ex-13

# kill server and client with sigkill
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -s 9 -p htmlUnit $defsBase/examples/fin/htmlunit/ex-1
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -s 9 -p htmlUnit $defsBase/examples/fin/htmlunit/ex-13
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -s 9 -p htmlUnit -e docker $defsBase/examples/fin/htmlunit/ex-1
hammer -n two-$((seq++)) -r 25 -m 4 -c 1 -k 0,4 -s 9 -p htmlUnit -e docker $defsBase/examples/fin/htmlunit/ex-13
