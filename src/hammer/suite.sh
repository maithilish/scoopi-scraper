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


# default -n 25 -s 1 -i 1 -k 1

hammer -r one-1 $defsBase/fin-ex-13-ext
hammer -r one-2 $defsBase/examples/fin/jsoup/ex-1
hammer -r one-3 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r one-4 $defsBase/examples/fin/jsoup/ex-13
hammer -r one-5 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13

# kill 2 -n 25 -s 1 -i 1 -k 2 -e docker

hammer -r twin-1 -s 1 -n 25 -k 2 -e docker $defsBase/fin-ex-13-ext
hammer -r twin-2 -s 1 -n 25 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -r twin-3 -s 1 -n 25 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r twin-4 -s 1 -n 25 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-13
hammer -r twin-5 -s 1 -n 25 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-13

# fine - increment kill by 0.25 seconds

hammer -r one-fine-1 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-1
hammer -r one-fine-2 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-13

hammer -r twin-fine-1 -s 1 -n 100 -i 0.25 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -r twin-fine-2 -s 1 -n 100 -i 0.25 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r twin-fine-3 -s 1 -n 100 -i 0.25 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-13
hammer -r twin-fine-4 -s 1 -n 100 -i 0.25 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-13
