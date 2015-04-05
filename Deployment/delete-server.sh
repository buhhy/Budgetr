#!/bin/bash

cur_dir="$(dirname "$0")"
cd $cur_dir
source settings.sh
if [ -d "$server_folder" ]; then
    echo "Deleting server folder at $(pwd)/$server_folder"
    rm -rf $server_folder
else
    echo "Server folder $(pwd)/$server_folder does not exist"
fi
if [ -f "$server_package" ]; then
    echo "Deleting server package at $(pwd)/$server_package"
    rm $server_package
else
    echo "Server package $(pwd)/$server_package does not exist"
fi
