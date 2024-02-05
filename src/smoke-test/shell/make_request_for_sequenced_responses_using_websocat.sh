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
  echo "$smoke_test_connection_response"
  exit 0
fi

websocat --text --exit-on-eof --protocol "zumba" -q -uU tcp-l:127.0.0.1:1234 reuse-raw:$1://$2:$3/ws/hello-world/2 --max-messages-rev 1&

smoke_test_sequenced_response=$(echo "Hello, World!" | nc 127.0.0.1 1234 2> /dev/null; echo  $?)
if [ "$smoke_test_sequenced_response" != "world-0" ]
then
  echo "WebSocket request to $2:$3 failed, got: $smoke_test_sequenced_response ... Exiting with status 1 ... "
  exit 1
else
  echo "$smoke_test_sequenced_response"
  exit 0
fi

smoke_test_sequenced_response=$(echo "Hello, World!" | nc 127.0.0.1 1234 2> /dev/null; echo  $?)
if [ "$smoke_test_sequenced_response" != "world-1" ]
then
  echo "WebSocket request to $2:$3 failed, got: $smoke_test_sequenced_response ... Exiting with status 1 ... "
  exit 1
else
  echo "$smoke_test_sequenced_response"
  exit 0
fi
