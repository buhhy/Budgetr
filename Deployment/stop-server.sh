#!/bin/bash

cur_dir="$(dirname "$0")"
cd $cur_dir
source settings.sh
if [ -d "$server_folder" ]; then
    cd $server_folder
    if [ -f "RUNNING_PID" ]; then
        echo "Killing server running on process $(cat RUNNING_PID)"
        cat RUNNING_PID | xargs kill
    else
        echo "Server isn't running"
    fi
else
    echo "Server does not exist"
fi
