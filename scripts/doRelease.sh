#!/bin/bash

# Stop at first command failure
set -e

read -p "Do you really want to create a release based on the current state (y/n)? "

# Check if prompted to continue
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Exiting..."
    exit 1
fi

# Check if the input parameter RELEASE_VERSION is valid
RELEASE_VERSION="$1"
if [[ ! $RELEASE_VERSION =~ ^([0-9]+\.[0-9]+\.[0-9])$ ]]; then
    echo "Error: RELEASE_VERSION must be in X.Y.Z format, but was $RELEASE_VERSION"
    exit 1
fi

# Check if the input parameter DEVELOPMENT_VERSION is valid
DEVELOPMENT_VERSION="$2"
if [[ ! $DEVELOPMENT_VERSION =~ ^([0-9]+\.[0-9]+\.[0-9])(\-SNAPSHOT)$ ]]; then
    echo "Error: DEVELOPMENT_VERSION must be in X.Y.Z-SNAPSHOT format, but was $DEVELOPMENT_VERSION"
    exit 1
fi

SCRIPTDIR=`dirname "$0"`

pushd $SCRIPTDIR/..

mvn release:clean
mvn release:prepare --batch-mode -DreleaseVersion=$RELEASE_VERSION -DdevelopmentVersion=$DEVELOPMENT_VERSION
mvn release:perform --batch-mode

popd

