#!/usr/bin/env bash

script_path=$(readlink -f "$0")
export CLEANUP_SCRIPTS_DIR=$(dirname "$script_path")
export CLEANUP_HOME=$(dirname "$CLEANUP_SCRIPTS_DIR")
export CLEANUP_LIB_DIR=$CLEANUP_HOME/lib
export CLEANUP_CONFIG_DIR=$CLEANUP_HOME/conf
export CLEANUP_TOKEN_DIR=$CLEANUP_HOME/token

lib_path=$(ls $CLEANUP_LIB_DIR/* | tr '\n' ':' | sed 's/:*$//' )
main_class='org.example.GmailQuickstart'

java \
--class-path "$lib_path" \
"$main_class"
