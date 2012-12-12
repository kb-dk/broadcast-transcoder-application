#!/bin/bash

collection=${1}

SCRIPT_PATH=$(dirname $(readlink -f $0))
CLASSPATH="$SCRIPT_PATH/../lib/*"

## This is 2012-04-01
INITIAL_TIMESTAMP=1333231200000

debug=1

#The number of worker processes. If this is changed from a previous run then the
#cleanup loop needs to be executed by hand.
WORKERS=2


#These are external machines on which to run the transcoding process by ssh eg user1@encoder1 ....
#This feature is not currently used
machines=( "machine1" "machine2" )

#Cleanup from previous run
for ((i=0;i<$WORKERS;i++)); do
            workerfile="${i}.$collection.workerFile"
            if [ -e  $workerfile ]; then
              uuid=$(cat $workerfile|cut -d ' ' -f2)
              timestamp=$(cat $workerfile|cut -d ' ' -f3)
              machine=$(cat $workerfile|cut -d ' ' -f4)

              lockfile "$SCRIPT_PATH/../fails.lock"
                      echo "$uuid   $timestamp" >> $SCRIPT_PATH/../fails
              rm -f "$SCRIPT_PATH/../fails.lock"

              rm $workerfile
              ##TODO do we need a $machine parameter to this call?
              $SCRIPT_PATH/cleanupUnfinished.sh $uuid $timestamp $machine
            fi
done
rm -f $SCRIPT_PATH/../*.lock *.lock *workerFile

progressFile="$SCRIPT_PATH/../$collection.progress"
if [ ! -e $progressFile ]; then
    echo $INITIAL_TIMESTAMP > $progressFile
fi

#get list of changes from queryChanges with progress timestamp as input
timestamp=$(cat $progressFile | tail -1)
changes=$(mktemp)
$SCRIPT_PATH/queryChanges.sh $timestamp $collection > $collection.$changes

#cut list into pid/timestamp sets
#iterate through list,

echo "number of programs to transcode is $(cat $changes | wc -l)"

machineIndex=0
while read line; do
    uuid=$(echo $line | cut -d' ' -f1)
    time=$(echo $line | cut -d' ' -f2)
        #Skip until a line starts with uuid
    if [[ $uuid != uuid:* ]]; then
        continue
    fi
    while [ 1 ]; do
            [ $debug = 1 ] && echo "$uuid: Starting allocation to worker for uuid $uuid"
            for ((i=0;i<$WORKERS;i++)); do
                    workerfile="${i}$collection.workerFile"
                    [ $debug = 1 ] && echo "$uuid: Attempting to get lock on $workerfile"
                    lockfile "$workerfile.lock"
                    if [ ! -e $workerfile ]; then
                        [ $debug = 1 ] && echo "$uuid: $workerfile does not exist, creating"
                        touch $workerfile
                    fi

                    pid=$(cat $workerfile|cut -d' ' -f1)
                    [ $debug = 1 ] && echo "$uuid: Got pid '$pid' from $workerfile"
                    if [ "$pid" == "" ]; then
                        pid="0" #somthing that will nto match in the grep
                    fi

                    ps ax | grep "^\s*$pid " &> /dev/null
                    found=$?

                    if [ $found != 0 ]; then #ie this pid is free
                        machine=${machines[$machineIndex]}
                        [ $debug = 1 ] && echo "$uuid: Did not find found '$pid' among the running processes"
                        [ $debug = 1 ] && echo "$uuid: Starting transcoding for $uuid and $time on $machine"
                        ##TODO logic for either transcodeProgram or transcodeReklame here
                        if [ $collection == "radiotv" ]
                        then
                          $SCRIPT_PATH/transcodeFile.sh $uuid $time $machine &
                        elif [ $collection == "reklamer ]
                          $SCRIPT_PATH/transcodeReklame.sh $uuid $time $machine &
                        else
                          echo "Unknown collection $collection"
                          exit 4
                        fi
                        echo "$! $uuid $time $machine" > $workerfile
                            #increment the machine index
                            ((machineIndex++))
                            max=${#machines[*]}
                            [ $machineIndex -ge $max ] && machineIndex=0

                        [ $debug = 1 ] && echo "$uuid: writing info to $workerfile"
                        [ $debug = 1 ] && echo "$uuid: releasing lock on $workerfile and go to next line"
                        rm -f "$workerfile.lock"
                        break 2
                    fi
                    [ $debug = 1 ] && echo "$uuid: releasing lock on $workerfile"
                    rm -f "$workerfile.lock"
            done
            sleep 10s
            echo
    done
    echo
done < $changes

rm $changes


