# Test Suite - hammer multiple example defs  

workDir=/tmp/hammer
logFile=$workDir/hammer.log
errorLogFile=$workDir/error.log
exampleDefs=defs/examples

hammer() {
    hammer.sh $@ 2>$errorLogFile | tee -a $logFile
}

rm -rf $workDir
mkdir -p $workDir
touch $logFile

# default -n 25 -s 1 -i 1

hammer -r run-1 defs/fin-ex-13-ext
hammer -r run-2 defs/examples/fin/jsoup/ex-1
hammer -r run-3 -p htmlunit defs/examples/fin/htmlunit/ex-1
hammer -r run-4 defs/examples/fin/jsoup/ex-13
hammer -r run-5 -p htmlunit defs/examples/fin/htmlunit/ex-13

# fine - increment kill by 0.25 seconds

#hammer -r run-6 -n 100 -i 0.25 defs/fin-ex-13-ext
#hammer -r run-7 -n 100 -i 0.25 defs/examples/fin/jsoup/ex-1
#hammer -r run-8 -n 100 -i 0.25 -p htmlunit defs/examples/fin/htmlunit/ex-1
#hammer -r run-9 -n 100 -i 0.25 defs/examples/fin/jsoup/ex-13
#hammer -r run-10 -n 100 -i 0.25 -p htmlunit defs/examples/fin/htmlunit/ex-13
