#!/bin/bash
#
# Pull the changelog data for the current (or specified) version of the mod
# Usage: scripts/extract_changes.sh [<version>]
# Default version is the version in gradle.properties
#

CUR_VER=$(grep mod_version_massive gradle.properties | cut -d= -f2).$(grep mod_version_major gradle.properties | cut -d= -f2).$(grep mod_version_minor gradle.properties | cut -d= -f2)
VER=${1:-$CUR_VER}

show=0
while IFS= read l; do
  if [[ "$l" =~ ^#[[:space:]] && $show -eq 1 ]]; then
    exit 0
  elif [[ "$l" =~ ^##[[:space:]]([0-9]+\.[0-9]+\.[0-9]+) ]]; then
		v=${BASH_REMATCH[1]}
		if [ "$v" == "$VER" ]; then
			show=1
		elif [ $show -eq 1 ]; then
			exit 0
		fi
	fi
	if [ $show -eq 1 ]; then
		echo "$l"
	fi
done < Changelog.md
