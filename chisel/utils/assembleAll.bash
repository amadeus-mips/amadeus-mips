#!/usr/bin/env bash
# shellcheck disable=SC2044
for i in $(find ../testMemFile/"$1" -type f -name "*.asm") ; do
  FULLNAME=$i
  k=${FULLNAME%.*}
  echo "$k"
  java -jar ~/Desktop/Mars4_5.jar mc CompactTextAtZero a dump .text HexText ${k}.txt  $i
done