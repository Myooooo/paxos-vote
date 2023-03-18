#!/bin/bash
echo Testing with 7 nodes: 1 proposer, 6 acceptors
echo

pkill -f "java"
mkdir logs

echo Starting communication manager...
java ds.assignment3.PaxosMain 4567 7 >./logs/server.txt &
sleep 1

echo Starting learner
java ds.assignment3.PaxosNode L1 IMMEDIATE LEARNER 4567 7 >./logs/l1.txt &
sleep 1

echo Starting nodes
java ds.assignment3.PaxosNode M1 IMMEDIATE PROPOSER 4567 7 >./logs/m1.txt &
java ds.assignment3.PaxosNode M2 MEDIUM ACCEPTOR 4567 7 >./logs/m2.txt &
java ds.assignment3.PaxosNode M3 MEDIUM ACCEPTOR 4567 7 >./logs/m3.txt &
java ds.assignment3.PaxosNode M4 MEDIUM ACCEPTOR 4567 7 >./logs/m4.txt &
java ds.assignment3.PaxosNode M5 MEDIUM ACCEPTOR 4567 7 >./logs/m5.txt &
java ds.assignment3.PaxosNode M6 MEDIUM ACCEPTOR 4567 7 >./logs/m6.txt &
java ds.assignment3.PaxosNode M7 MEDIUM ACCEPTOR 4567 7 >./logs/m7.txt &

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
echo Showing learner log...
cat ./logs/l1.txt