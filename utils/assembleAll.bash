#!/usr/bin/env bash
for i in `find ../testMemFile/$1 -type f -name "*.asm"` ; do
  k=${${i}%.*}
  echo $k
  java -jar Mars4_5.jar mc CompactTextAtZero a dump .text HexText ${k}.txt  $i
done