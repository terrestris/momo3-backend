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

# Check if the input parameter RELEASE_DESCRIPTION is valid
RELEASE_DESCRIPTION="$3"
if [[ -z $RELEASE_DESCRIPTION ]]; then
    echo "Error: RELEASE_DESCRIPTION must be set"
    exit 1
fi

# Check if the input parameter RELEASE_DESCRIPTION is valid
GITLAB_TOKEN="$4"
if [[ -z $GITLAB_TOKEN ]]; then
    echo "Error: GITLAB_TOKEN must be set"
    exit 1
fi

COMMIT_PREFIX="[AUTOCOMMIT]"
RELEASE_COMMIT_MSG="$COMMIT_PREFIX Set version for the release ($RELEASE_VERSION)"
PREPARE_NEXT_DEV_ITERATION_COMMIT_MSG="$COMMIT_PREFIX Set version for the next development iteration ($DEVELOPMENT_VERSION)"

SCRIPTDIR=`dirname "$0"`

pushd $SCRIPTDIR/..

# Check for any local modifications
mvn scm:check-local-modification

# Set the release version
mvn versions:set -DnewVersion="$RELEASE_VERSION"

# Build/Verify the application
mvn verify -Dskip-build-javascript-resources=false -Dskip-userdoc-regeneration=false -Dgitlab-token=$GITLAB_TOKEN

# Commit the release version (checkin I of II)
mvn scm:checkin -Dmessage="$RELEASE_COMMIT_MSG"

# Create the tagged release on GitHub
mvn github-release:release -DreleaseDescription="$RELEASE_DESCRIPTION"

# Set the next development version
mvn versions:set -DnewVersion="$DEVELOPMENT_VERSION"

# Commit the next development version (checkin II of II)
mvn scm:checkin -Dmessage="$PREPARE_NEXT_DEV_ITERATION_COMMIT_MSG"

popd
