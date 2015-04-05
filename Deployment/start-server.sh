#!/bin/bash

cur_dir="$(dirname "$0")"
cd $cur_dir
source settings.sh
mkdir -p $log_folder
echo "Starting server..."
nohup $server_folder/bin/budgetr -mem $server_memory -J-server 1> $log_folder/output.log 2> $log_folder/error.log & echo $!
