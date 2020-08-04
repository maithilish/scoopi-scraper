# mulitple run cli scoopi cluster

workDir=/tmp/hammer
logFile=$workDir/hammer.log
exampleDefs=defs/examples

hammer() {
    hammer.sh $@ 2>&1 | tee -a $logFile
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
