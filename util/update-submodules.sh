#!/usr/bin/env bash

# common variables
BASEDIR=$(dirname $0)
WORKDIR=$(pwd -P)
cd $BASEDIR
BASEDIR=$(pwd -P)
cd $WORKDIR


REPOBASEDIR="$BASEDIR/.."
WEBAPPDIR="$REPOBASEDIR/src/main/webapp"
BASEUPSTREAM="https://github.com/terrestris/"

# load reusable functions
source $REPOBASEDIR/util/functions.bash.sh

function is_clean_git_repo {
    if test -n "$(git status --porcelain)"; then
        return 1
    else
        return 0
    fi
}

dirs=""
dirs="$dirs $WEBAPPDIR/login"
dirs="$dirs $WEBAPPDIR/admin"
dirs="$dirs $WEBAPPDIR/client"
dirs="$dirs $WEBAPPDIR/lib/BasiGX"
dirs="$dirs $WEBAPPDIR/lib/geoext3"

upstreams=""
upstreams="$upstreams $BASEUPSTREAM/momo3-login"
upstreams="$upstreams $BASEUPSTREAM/momo3-admin"
upstreams="$upstreams $BASEUPSTREAM/momo3-frontend"
upstreams="$upstreams https://github.com/terrestris/BasiGX"
upstreams="$upstreams https://github.com/geoext/geoext3"
upstreams=($upstreams)

index=0;

title "Update common submodules"

for dir in $dirs; do
    echo -n "Updating `basename $dir`: "
    cd $dir
    is_clean_git_repo
    if [[ "$?" -eq "0" ]]; then
        upstream=${upstreams[$index]}
        git checkout master > /dev/null 2>&1
        chkprogress "checkout master"
        git pull $upstream master > /dev/null 2>&1
        chkprogress "pull from upstream master"
    else
        echo -n "Skipping, repo is dirty"
    fi
    cd $WORKDIR
    index=$(( index + 1))
    echo
done

cd $REPOBASEDIR

section "Status of main repository:"
echo

git status

cd $WORKDIR

exit 0
