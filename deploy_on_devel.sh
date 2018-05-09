#!/usr/bin/env bash

set -e
set -x
SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))

cd $SCRIPT_DIR

develHost="bta@iapetus"

mvn clean install -Psbprojects-nexus -DskipTests

archive=$(basename broadcast-transcoder-commandline/target/broadcast-transcoder-commandline-*-zip-package.tar.gz)

scp broadcast-transcoder-commandline/target/$archive $develHost:.

#TODO perhaps delete existing install first?
ssh $develHost "tar -xvzf $archive --directory /home/bta/bta --strip 1"

ssh $develHost 'rm -f streamingContent/*.mp3'
ssh $develHost 'rm -f streamingContent/*.flv'
ssh $develHost 'rm -f imageDirectory/*.png'

ssh $develHost 'rm -f logs/* || true'

#Initialisation
#ssh $develHost "psql -d bta-devel -c 'delete from broadcasttranscodingrecord;'"
#ssh $develHost 'bta/bin/enqueueJobs.sh Broadcast 0'
#ssh $develHost 'bta/bin/queryChangesDoms.sh Broadcast > ~/queue$(date +"%y%m%d").txt'
ssh $develHost 'head -n4 ~/queue$(date +"%y%m%d").txt > ~/queue.txt'
ssh $develHost 'bta/bin/transcode-master.sh start -h localhost -n 1 -j ~/queue.txt -v'


# Stop trancodering:
# bta/bin/transcode-master.sh stop
# ps -ef | grep transcoder | grep -v grep| sed 's/ \+/ /g' | cut -d' ' -f2 | xargs -r kill