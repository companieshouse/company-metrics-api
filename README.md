company-metrics-api
=========================

company-metrics-api is responsible for 
1. Persisting company metrics collection to database
2. Company Metrics API has a single RESTful endpoint to GET company metrics and a single RPC (action based) endpoint to force a metrics recalculation
3. Company Metrics API is a store for few different types of resource mterics: charges, officers and pscs.

## Development

Common commands used for development and running locally can be found in the Makefile, each make target has a
description which can be listed by running `make help`

```text
Target               Description
------               -----------
all                  Calls methods required to build a locally runnable version, typically the build target
build                Pull down any dependencies and compile code into an executable if required
clean                Reset repo to pre-build state (i.e. a clean checkout state)
deps                 Install dependencies
package              Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the build target being run before package
sonar                Run sonar scan
test                 Run all test-* targets (convenience method for developers)
test-integration     Run integration tests
test-unit            Run unit tests

```

## Running mongodb locally
From root folder of this project run ```docker-compose up -d```
