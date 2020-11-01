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

# -n 25 -s 1 -i 1, -k 9 (SIGKILL) or 15 (SIGTERM)

hammer -r one-1 $defsBase/examples/fin/jsoup/ex-1
hammer -r one-2 $defsBase/examples/fin/jsoup/ex-13
hammer -r one-3 -k 9 $defsBase/examples/fin/jsoup/ex-1
hammer -r one-4 -k 9 $defsBase/examples/fin/jsoup/ex-13
hammer -r one-5 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r one-6 -k 9 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r one-7 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
hammer -r one-8 $defsBase/fin-ex-13-ext

hammer -r doc-one-1 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -r doc-one-2 -e docker $defsBase/examples/fin/jsoup/ex-13
hammer -r doc-one-3 -e docker -k 9 $defsBase/examples/fin/jsoup/ex-1
hammer -r doc-one-4 -e docker -k 9 $defsBase/examples/fin/jsoup/ex-13
hammer -r doc-one-5 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r doc-one-6 -e docker -k 9 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r doc-one-7 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
hammer -r doc-one-8 -e docker $defsBase/fin-ex-13-ext

# kill 2 

hammer -r two-1 -c 2 $defsBase/examples/fin/jsoup/ex-1
hammer -r two-2 -c 2 $defsBase/examples/fin/jsoup/ex-13
hammer -r two-3 -c 2 -k 9 $defsBase/examples/fin/jsoup/ex-1
hammer -r two-4 -c 2 -k 9 $defsBase/examples/fin/jsoup/ex-13
hammer -r two-5 -c 2 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r two-6 -c 2 -k 9 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r two-7 -c 2 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
hammer -r two-8 -c 2 $defsBase/fin-ex-13-ext

hammer -r doc-two-1 -e docker -c 2 $defsBase/examples/fin/jsoup/ex-1
hammer -r doc-two-2 -e docker -c 2 $defsBase/examples/fin/jsoup/ex-13
hammer -r doc-two-3 -e docker -c 2 -k 9 $defsBase/examples/fin/jsoup/ex-1
hammer -r doc-two-4 -e docker -c 2 -k 9 $defsBase/examples/fin/jsoup/ex-13
hammer -r doc-two-5 -e docker -c 2 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r doc-two-6 -e docker -c 2 -k 9 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r doc-two-7 -e docker -c 2 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
hammer -r doc-two-8 -e docker -c 2 $defsBase/fin-ex-13-ext

# fine - increment kill by 0.25 seconds

hammer -r one-fine-1 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-1
hammer -r twin-fine-1 -s 1 -n 100 -i 0.25 -c 2 -e docker $defsBase/examples/fin/jsoup/ex-1

hammer -r one-fine-2 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-13
hammer -r twin-fine-2 -s 1 -n 100 -i 0.25 -c 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r twin-fine-3 -s 1 -n 100 -i 0.25 -c 2 -e docker $defsBase/examples/fin/jsoup/ex-13
hammer -r twin-fine-4 -s 1 -n 100 -i 0.25 -c 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
