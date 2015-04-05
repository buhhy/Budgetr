#!/bin/bash

cur_dir="$(dirname "$0")"
cd $cur_dir
source settings.sh
mkdir -p $server_folder
echo "Unpacking server package to directory $(pwd)/$server_folder"
unzip $server_package -d $server_folder
# unnest the server folder
cd $server_folder
nested_folder=$(ls | sort -n | head -1)
shopt -s dotglob nullglob
mv $nested_folder/* .
rmdir $nested_folder
echo "Unnested folder $(pwd)/$nested_folder"

