#!/bin/bash

# Exit script if you try to use an uninitialized variable.
set -o nounset

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

echo ""
echo "#####################################################################"
echo "## WebSocket request is being made to $1://$2:$3"
echo "#####################################################################"

# https://gist.github.com/htp/fbce19069187ec1cc486b594104f01d0#gistcomment-2638079
smoke_test_connection_response=$(websocat \
  -q -uU \
  --protocol "zumba" \
  $1://$2:$3/ws/hello-world/2 2> /dev/null; echo  $?)

if [ "$smoke_test_connection_response" != "0" ]
then
  echo "WebSocket request to $2:$3 failed, exiting with status 1 ... "
  exit 1
else
  echo "Got success status: $smoke_test_connection_response"
fi


for idx in $(seq 0 4); do echo "Hello, World!"; sleep 1; done | websocat --protocol "zumba" $1://$2:$3/ws/hello-world/2 > sequenced_responses.txt
echo ""
echo "#####################################################################"
echo "## WebSocket sequenced responses received from $1://$2:$3/ws/hello-world/2"
echo "#####################################################################"

if grep -xq "world-0" "sequenced_responses.txt"; then 
    echo "Received 'world-0' from the WebSocket server"
else 
    echo "WebSocket response from $2:$3 is incomplete, could not receive 'world-0' ... Exiting with status 1 ... "
    exit 1
fi

if grep -xq "world-1" "sequenced_responses.txt"; then 
    echo "Received 'world-1' from the WebSocket server"
else 
    echo "WebSocket response from $2:$3 is incomplete, could not receive 'world-1' ... Exiting with status 1 ... "
    exit 1
fi

if grep -xq "world-2a,world-2b,world-2c,world-2d,world-2e" "sequenced_responses.txt"; then 
    echo "Received 'world-2a,world-2b,world-2c,world-2d,world-2e' from the WebSocket server"
else 
    echo "WebSocket response from $2:$3 is incomplete, could not receive 'world-2a,world-2b,world-2c,world-2d,world-2e' ... Exiting with status 1 ... "
    exit 1
fi

if grep -xq "world-3" "sequenced_responses.txt"; then 
    echo "Received 'world-3' from the WebSocket server"
else 
    echo "WebSocket response from $2:$3 is incomplete, could not receive 'world-3' ... Exiting with status 1 ... "
    exit 1
fi

