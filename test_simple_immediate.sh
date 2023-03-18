#!/bin/bash
echo Testing with 3 nodes: 1 proposer, 2 acceptors
echo

pkill -f "java"
mkdir logs

echo Starting communication manager...
java ds.assignment3.PaxosMain 4567 3 >./logs/server.txt &
sleep 1

echo Starting learner
java ds.assignment3.PaxosNode L1 IMMEDIATE LEARNER 4567 3 >./logs/l1.txt &
sleep 1

echo Starting nodes
java ds.assignment3.PaxosNode M1 IMMEDIATE PROPOSER 4567 3 >./logs/m1.txt &
java ds.assignment3.PaxosNode M2 IMMEDIATE ACCEPTOR 4567 3 >./logs/m2.txt &
java ds.assignment3.PaxosNode M3 IMMEDIATE ACCEPTOR 4567 3 >./logs/m3.txt &

echo Running...

start=$(($(date +%s%N)/1000000))

for job in `jobs -p`
do
    wait $job || let "FAIL+=1"
done
echo $FAIL

finish=$(($(date +%s%N)/1000000))
elapsed=$(( finish - start ))

echo Paxos run finished
echo "Completed in $elapsed ms"
echo Showing learner log
cat ./logs/l1.txt