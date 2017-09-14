#!/usr/bin/env bash

##################################################################
#
# Central functions commonly used in bash programms
#
# author: Christian Mayer
# author: Marc Jansen
#
##################################################################

STYLE="false"

function textweight_bold {
    if [ "$STYLE" = "true" ]
    then
        txtbold=$(tput bold) # Bold
        echo -n "${txtbold}"
    fi
}
function textcolor_red {
    if [ "$STYLE" = "true" ]
    then
        txtred=$(tput setaf 1) # Red
        echo -n "${txtred}"
    fi
}
function textcolor_green {
    if [ "$STYLE" = "true" ]
    then
        txtgrn=$(tput setaf 2) # Green
        echo -n "${txtgrn}"
    fi
}
function textcolor_yellow {
    if [ "$STYLE" = "true" ]
    then
        txtylw=$(tput setaf 3) # Yellow
        echo -n "${txtylw}"
    fi
}
function textcolor_blue {
    if [ "$STYLE" = "true" ]
    then
        txtblu=$(tput setaf 4) # Blue
        echo -n "${txtblu}"
    fi
}
function textcolor_purple {
    if [ "$STYLE" = "true" ]
    then
        txtpur=$(tput setaf 5) # Purple
        echo -n "${txtpur}"
    fi
}
function textcolor_cyan {
    if [ "$STYLE" = "true" ]
    then
        txtcyn=$(tput setaf 6) # Cyan
        echo -n "${txtcyn}"
    fi
}
function textcolor_white {
    if [ "$STYLE" = "true" ]
    then
        txtwht=$(tput setaf 7) # White
        echo -n "${txtwht}"
    fi
}
function textcolor_reset {
    if [ "$STYLE" = "true" ]
    then
        txtrst=$(tput sgr0) # Text reset
        echo -n "${txtrst}"
    fi
}


# echos an empty line as spacer
function spacerline {
    echo "";
}

# echos a given string ($1) n times ($2)
function repeatecho {
    for (( c=1; c<=$2; c++ ))
    do
        echo -n $1
    done
}
# repeats a given string ($1) n times ($2) and returns it
function repeat {
    STR=""
    for (( c=1; c<=$2; c++ ))
    do
        STR="$STR$1"
    done
    return $STR
}

# Prints out the given argument as title of a section surrounded with box
# characters
function section {
    spacerline
    textcolor_blue
    #box $1
    echo $1
    textcolor_reset
}

function error {
    textcolor_red
    echo -n "     ERROR: $1"
    echo;
    textcolor_reset
    exit 1
}

function warning {
    textcolor_yellow
    echo -n "     WARNING: $1"
    echo;
    textcolor_reset
}

function ok {
    textcolor_green
    echo -n "$1"
    echo;
    textcolor_reset
}

function success {
    ok "...Success."
}

# Prints out the uppercased argument as title of a script surrounded with box
# characters
function title {
    thickbox `echo "$1" | tr [a-z] [A-Z]`
    #`echo $1 | tr [a-z] [A-Z]`
    #echo "$1" | tr [a-z] [A-Z]
}

# prints a box around the argument, e.g.
#
# ┌──────┐
# │ test │
# └──────┘
function box () {
    str="$@"
    len=$((${#str}+2))
    echo -n " ┌"
    for i in $(seq $len); do echo -n '─'; done;
    echo -n "┐"
    echo; echo " │ "$str" │";
    echo -n " └"
    for i in $(seq $len); do echo -n '─'; done;
    echo -n "┘"
    echo
}

# prints a thick box around the argument, e.g.
#
# ┏━━━━━━┓
# ┃ test ┃
# ┗━━━━━━┛
function thickbox () {
    str="$@"
    len=$((${#str}+2))
    echo -n " ┏"
    for i in $(seq $len); do echo -n '━'; done;
    echo -n "┓"
    echo; echo " ┃ "$str" ┃";
    echo -n " ┗"
    for i in $(seq $len); do echo -n '━'; done;
    echo -n "┛"
    echo
}

# prints a box of double and single lines around the argument, e.g.
#
# ╒══════╕
# │ test │
# ╘══════╛
function halfdoublebox () {
    str="$@"
    len=$((${#str}+2))
    echo -n " ╒"
    for i in $(seq $len); do echo -n '═'; done;
    echo -n "╕"
    echo; echo " │ "$str" │";
    echo -n " ╘"
    for i in $(seq $len); do echo -n '═'; done;
    echo -n "╛"
    echo
}

# prints an other box of double and single lines around the argument, e.g.
#
# ╓──────╖
# ║ test ║
# ╙──────╜
function halfdoublebox2 () {
    str="$@"
    len=$((${#str}+2))
    echo -n " ╓"
    for i in $(seq $len); do echo -n '─'; done;
    echo -n "╖"
    echo; echo " ║ "$str" ║";
    echo -n " ╙"
    for i in $(seq $len); do echo -n '─'; done;
    echo -n "╜"
    echo
}

# prints a box with double lines around the argument, e.g.
#
# ╔══════╗
# ║ test ║
# ╚══════╝
function doublebox () {
    str="$@"
    len=$((${#str}+2))
    echo -n " ╔"
    for i in $(seq $len); do echo -n '═'; done;
    echo -n "╗"
    echo; echo " ║ "$str" ║";
    echo -n " ╚"
    for i in $(seq $len); do echo -n '═'; done;
    echo -n "╝"
    echo
}

# Prints out the given argument as subtask of a section with a leading dash
function subsection {
    #spacerline
    textweight_bold
    echo -n "  - "
    echo -n $1
    textcolor_reset
    spacerline
    echo -n "    "
}

# Prints out the given argument and a timestamp usefull for logging
function consoleLog {
    echo ""
    echo -n "[ "
    echo -n $1 $(date +'%Y-%m-%d %H:%M:%S %z')
    echo " ]"
    echo ""
}

function chkcmd {
    which $1 >/dev/null
    if [ $? -ne 0 ]; then
        error "Program '$1' not found."
    fi
}

function chkprogress {
    if [ $? -eq 0 ]; then
        echo -n "✔ "
    else
        error "$1 failed"
    fi
}

function chkcontainserror {
    if grep -iq "Error" $1
    then
        echo
        cat $1
        echo
        error "$2"
    fi
}

function abspath {
    RELPATH="${1}"
    PYTHONPROG="import os; print os.path.abspath(\"${RELPATH}\")"
    ABSPATH="`python -c \"${PYTHONPROG}\"`"
    echo ${ABSPATH}
}
