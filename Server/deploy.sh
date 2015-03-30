#!/bin/bash
url=ec2-user@www.budgie.works
folder=/srv/servers/budgetr
./activator dist
ssh $url "$folder/stop-server.sh;$folder/delete-server.sh"
scp target/universal/budgetr-1.0-SNAPSHOT.zip ec2-user@www.budgie.works:/srv/servers/budgetr/budgetr.zip
ssh $url "$folder/unpack-server.sh;$folder/start-server.sh"
