#!/bin/bash

errors=0

# if options are not set, then set defaults
[ -z ${runName+x} ] && runName="run-a"
[ -z ${numOfRuns+x} ] && numOfRuns=25
[ -z ${killList+x} ] && killList=0
[ -z ${killSignal+x} ] && killSignal=15
[ -z ${crashBeginAt+x} ] && crashBeginAt=1
[ -z ${incrementBy+x} ] && incrementBy=1
[ -z ${parser+x} ] && parser="jsoup"
[ -z ${engine+x} ] && engine="java"
[ -z ${numOfServers+x} ] && numOfServers=3
[ -z ${numOfClients+x} ] && numOfClients=0

showHelp() {

    cat <<EOF

Usage: ${0##*/} [<options>] <defs dir>

Test Scoopi cluster consistency by repeatly run and crash nodes.

Options:
  -n, --name string           run name
  -r, --runs int              how many runs
  -k, --kills int list        list of indexes to kill (example: 0,2,3)
  -s, --signal int            kill signal 
  -b, --begin int             kill begin at (seconds)
  -i, --increment int,deci    increment kill seconds in each run
  -p, --parser string         parser (jsoup, htmUnit, default jsoup)
  -e, --engine string         engine (java, docker, default java)
  -m  --servers int           number of hazelcast servers (default 3)
  -c  --clients int           number of hazelcast clients (default 0)
  -h  --help                  display this help and exit    
EOF

}

die() {
    printf '%s\n' "$1" >&2
    exit 1
}

logError() {
    option=$1
    message=$2
    printf "  %s: %s\n" "$option" "$message" >&2
    ((errors += 1))
}

validateNumber() {

    local number=$1
    local regex=$2
    local min=$3
    local max=$4

    local valid=1
    ! [[ $number =~ $regex ]] && valid=0

    [ $valid -eq 1 ] && [[ $(echo "$number $min" | awk '{print ($1 < $2)}') == 1 ]] && valid=0
    [ $valid -eq 1 ] && [[ $(echo "$number $max" | awk '{print ($1 > $2)}') == 1 ]] && valid=0

    return $valid
}

## process options
while :; do
    case $1 in
    -h | -\? | --help)
        showHelp
        exit
        ;;
    -n | --name)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            runName=$2
            shift
        else
            logError $option "run name not specified"
        fi
        ;;
    -r | --runs)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            numOfRuns=$2
            shift
            message="not integer or out of range 1..100"
            $(validateNumber $numOfRuns '^[0-9]+$' 1 100) && logError $option "$message"
        else
            logError $option "how many runs not specified"
        fi
        ;;
    -k | --kills)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            killList=$2
            shift
            message="not index or comma delimited indexes (example: 2,4,5)"
            ! [[ $killList =~ ^([0-9])(,[0-9])*$ ]] && logError $option "$message"
        else
            logError $option "how many kills not specified"
        fi
        ;;
    -s | --signal)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            killSignal=$2
            shift
            message="not integer or out of range 1..15"
            $(validateNumber $killSignal '^[0-9]+$' 1 15) && logError $option "$message"
        else
            logError $option "kill signal not specified"
        fi
        ;;
    -b | --begin)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            crashBeginAt=$2
            shift
            message="not integer or out of range 1..20"
            $(validateNumber $crashBeginAt '^[0-9]+$' 1 20) && logError $option "$message"
        else
            logError $option "crash begin at not specified"
        fi
        ;;
    -b | --begin)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            crashBeginAt=$2
            shift
            message="not integer or out of range 1..20"
            $(validateNumber $crashBeginAt '^[0-9]+$' 1 20) && logError $option "$message"
        else
            logError $option "crash begin at not specified"
        fi
        ;;
    -i | --increment)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            incrementBy=$2
            shift
            message="not decimal or out of range 0.1..5"
            $(validateNumber $incrementBy '^[0-9]+([.][0-9]+)?$' 0.1 5) && logError $option "$message"
        else
            logError $option "increment by not specified"
        fi
        ;;
    -p | --parser)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            parser=$2
            shift
            message="should be jsoup or htmlUnit"
            [[ "$parser" != "jsoup" && "$parser" != "htmlUnit" ]] && logError $option "$message"
        else
            logError $option "parser not specified"
        fi
        ;;
    -e | --engine)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            engine=$2
            shift
            message="should be java or docker"
            [[ "$engine" != "java" && "$engine" != "docker" ]] && logError $option "$message"
        else
            logError $option "run engine not specified"
        fi
        ;;
    -m | --servers)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            numOfServers=$2
            shift
            message="not integer or out of range 1..5"
            $(validateNumber $numOfServers '^[0-9]+$' 1 5) && logError $option "$message"
        else
            logError $option "number of servers not specified"
        fi
        ;;
    -c | --clients)
        option=$1
        if [[ "$2" && "$2" != -* ]]; then
            numOfClients=$2
            shift
            message="not integer or out of range 1..5"
            $(validateNumber $numOfClients '^[0-9]+$' 1 5) && logError $option "$message"
        else
            logError $option "number of clients not specified"
        fi
        ;;

    --)
        shift
        break
        ;;
    -?*)
        option=$1
        logError $option "unknown option"
        ;;
    *)
        break
        ;;
    esac

    shift
done

defsDir=$@

[[ -z "$defsDir" ]] && logError "argument" "defs dir not specified"
[ ! -d "$defsDir" ] && logError "defs dir" "$defsDir not found"

[[ $errors -gt 0 ]] && die

defsDirBaseName=$(basename $defsDir)
parser+="Default"
# replace comma with space and create array of indexes
killIndexes=(${killList/,/ })

echo
echo "$(tput setaf 3)[$runName]$(tput sgr0) runs: $(tput setaf 3)$numOfRuns$(tput sgr0)  kill at: $(tput setaf 3)$crashBeginAt$(tput sgr0)  inc: $(tput setaf 3)$incrementBy$(tput sgr0)  kill list: $(tput setaf 3)[$killList] nodes$(tput sgr0)  kill SIG: $(tput setaf 3)$killSignal$(tput sgr0)"
echo "$(tput setaf 3)[$runName]$(tput sgr0) defs: $(tput setaf 3)$defsDirBaseName$(tput sgr0)  parser: $(tput setaf 3)$parser$(tput sgr0)  engine: $(tput setaf 3)$engine$(tput sgr0)  servers: $(tput setaf 3)$numOfServers$(tput sgr0)  clients: $(tput setaf 3)$numOfClients$(tput sgr0)"
echo
