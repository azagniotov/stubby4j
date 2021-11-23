#!/bin/bash

# Exit script if you try to use an uninitialized variable.
set -o nounset

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

echo ""
echo "#####################################################################"
echo "## Request is being made to $2:$3 over TLS v$1"
echo "#####################################################################"

smoke_test_response=$(curl \
  -X GET -H "Content-Type: application/json" \
  --tls-max $1 \
  --tlsv$1 \
  --cacert src/main/resources/ssl/openssl.downloaded.stubby4j.self.signed.v3.pem \
  --verbose \
  https://$2:$3/tests/smoke-test/1)

if [ "$smoke_test_response" != "OK" ]
then
  echo "TLS $1 request to $2:$3 failed, exiting with status 1 ... "
  exit 1
else
  echo "$smoke_test_response"
  exit 0
fi
