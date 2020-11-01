#!/bin/bash

# if option variables are not set, then set them
[ -z ${runName+x} ] && runName="run-a"
[ -z ${crashStartOffset+x} ] && crashStartOffset=1
[ -z ${incrementBy+x} ] && incrementBy=1
[ -z ${numOfTimes+x} ] && numOfTimes=25
[ -z ${parser+x} ] && parser="jsoupDefault"
[ -z ${killHowMany+x} ] && killHowMany=1
[ -z ${killSignal+x} ] && killSignal=15
[ -z ${runtime+x} ] && runtime="java"

# Reset getopts
OPTIND=1

# Usage info
show_help() {
    cat <<EOF
Usage: ${0##*/} [<options>] <defs dir>

Check consistency of Scoopi cluster by crashing a node at specified time.

    -r      name
    -n      number of runs
    -s      kill after s seconds
    -i      increment kill seconds in each run (integer or decimal)
    -p      parser (jsoup or htmlunit, default jsoup)
    -c      kill how many processes
    -k      kill signal (integer)
    -e      runtime (java or docker, default java)
    -h      display this help and exit    
EOF
}

show_help_exit() {
    echo
    show_help
    exit $1
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

# process options

while getopts "hr:n:s:i:p:c:e:k:" opt; do
    case $opt in
    r)
        runName=$OPTARG
        ;;
    n)
        numOfTimes=$OPTARG
        ;;
    s)
        crashStartOffset=$OPTARG
        ;;
    i)
        incrementBy=$OPTARG
        ;;
    c)
        killHowMany=$OPTARG
        ;;
    k)
        killSignal=$OPTARG
        ;;
    e)
        runtime=$OPTARG
        ;;
    p)
        if [[ $OPTARG == "jsoup" ]]; then
            parser="jsoupDefault"
        else
            parser="htmlUnitDefault"
        fi
        ;;
    h)
        show_help_exit 0
        ;;
    *)
        show_help_exit 1
        ;;
    esac
done

[[ -z "$@" ]] && show_help_exit 1 # no options and def

shift "$((OPTIND - 1))" # get parameters, discard options

[[ -z "$@" ]] && show_help_exit 1 # no parameters exit - no def dir

defsDir=$1
defsDirBaseName=$(basename $defsDir)

echo
echo "$(tput setaf 3)[$runName]$(tput sgr0) runs: $(tput setaf 3)$numOfTimes$(tput sgr0)  kill at: $(tput setaf 3)$crashStartOffset$(tput sgr0)  ++: $(tput setaf 3)$incrementBy$(tput sgr0)  kill: $(tput setaf 3)$killHowMany pids$(tput sgr0)  kill SIG: $(tput setaf 3)$killSignal$(tput sgr0)"
echo "$(tput setaf 3)[$runName]$(tput sgr0) defs: $(tput setaf 3)$defsDirBaseName$(tput sgr0)  parser: $(tput setaf 3)$parser$(tput sgr0)  runtime: $(tput setaf 3)$runtime$(tput sgr0)"
echo

nConstraint="error: option -n not integer or out of range 1..100"
$(validateNumber $numOfTimes '^[0-9]+$' 1 100) && echo $nConstraint && exit 1

sConstraint="error: option -s not integer or out of range 1..100"
$(validateNumber $crashStartOffset '^[0-9]+$' 1 100) && echo $sConstraint && exit 1

iConstraint="error: option -i not decimal or out of range 0.1..5"
$(validateNumber $incrementBy '^[0-9]+([.][0-9]+)?$' 0.1 5) && echo $iConstraint && exit 1

nConstraint="error: option -k not integer"
$(validateNumber $killSignal '^[0-9]+$' 1 15) && echo $nConstraint && exit 1

nConstraint="error: option -c not integer or out of range 1..2"
$(validateNumber $killHowMany '^[0-9]+$' 1 2) && echo $nConstraint && exit 1

[ ! -d $defsDir ] && echo "error: def dir not found, $defsDir" && exit 1
