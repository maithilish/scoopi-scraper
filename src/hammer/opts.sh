#!/bin/bash

# if option variables are not set, then set them
[ -z ${runName+x} ] && runName="run-a"
[ -z ${crashStartOffset+x} ] && crashStartOffset=1
[ -z ${incrementBy+x} ] && incrementBy=1
[ -z ${numOfTimes+x} ] && numOfTimes=25
[ -z ${parser+x} ] && parser="jsoupDefault"

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
    -p      parser (jsoup or htmlunit)
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

while getopts "hr:n:s:i:p:" opt; do
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

echo
echo "$(tput setaf 3)[$runName]$(tput sgr0), runs: $(tput setaf 3)$numOfTimes$(tput sgr0), kill start: $(tput setaf 3)$crashStartOffset$(tput sgr0), increment by: $(tput setaf 3)$incrementBy$(tput sgr0), parser: $parser, defs: $(tput setaf 3)$defsDir$(tput sgr0)"
echo

nConstraint="error: option -n not integer or out of range 1..100"
$(validateNumber $numOfTimes '^[0-9]+$' 1 100) && echo $nConstraint && exit 1

sConstraint="error: option -s not integer or out of range 1..100"
$(validateNumber $crashStartOffset '^[0-9]+$' 1 100) && echo $sConstraint && exit 1

iConstraint="error: option -i not decimal or out of range 0.1..5"
$(validateNumber $incrementBy '^[0-9]+([.][0-9]+)?$' 0.1 5) && echo $iConstraint && exit 1

[ ! -d $defsDir ] && echo "error: def dir not found, $defsDir" && exit 1
