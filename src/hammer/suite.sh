# Test Suite - hammer multiple example defs  

baseDir=$PWD
workDir=/tmp/hammer
logFile=$workDir/hammer.log
errorLogFile=$workDir/error.log
defsBase=defs

hammer() {
    script/hammer.sh $@ 2>$errorLogFile | tee -a $logFile
}

sudo rm -rf $workDir
mkdir -p $workDir
touch $logFile

# default -n 25 -s 1 -i 1 -k 1

#hammer -r run-1 $defsBase/fin-ex-13-ext
#hammer -r run-2 $defsBase/examples/fin/jsoup/ex-1
#hammer -r run-3 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
#hammer -r run-4 $defsBase/examples/fin/jsoup/ex-13
#hammer -r run-5 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13

# kill 2 -n 25 -s 1 -i 1 -k 2 -e docker

hammer -r run-1 -k 2 -e docker $defsBase/fin-ex-13-ext
hammer -r run-2 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-1
hammer -r run-3 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
hammer -r run-4 -k 2 -e docker $defsBase/examples/fin/jsoup/ex-13
hammer -r run-5 -k 2 -e docker -p htmlunit $defsBase/examples/fin/htmlunit/ex-13

# fine - increment kill by 0.25 seconds

#hammer -r run-6 -n 100 -i 0.25 $defsBase/fin-ex-13-ext
#hammer -r run-7 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-1
#hammer -r run-8 -n 100 -i 0.25 -p htmlunit $defsBase/examples/fin/htmlunit/ex-1
#hammer -r run-9 -n 100 -i 0.25 $defsBase/examples/fin/jsoup/ex-13
#hammer -r run-10 -n 100 -i 0.25 -p htmlunit $defsBase/examples/fin/htmlunit/ex-13

