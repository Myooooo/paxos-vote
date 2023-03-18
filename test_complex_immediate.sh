#!/bin/bash
echo Testing with 9 nodes: 1 proposer, 8 acceptors
echo

pkill -f "java"
mkdir logs

echo Starting communication manager...
java ds.assignment3.PaxosMain 4567 9 >./logs/server.txt &
sleep 1

echo Starting learner
java ds.assignment3.PaxosNode L1 IMMEDIATE LEARNER 4567 9 >./logs/l1.txt &
sleep 1

echo Starting nodes
java ds.assignment3.PaxosNode M1 IMMEDIATE PROPOSER 4567 9 >./logs/m1.txt &
java ds.assignment3.PaxosNode M2 IMMEDIATE ACCEPTOR 4567 9 >./logs/m2.txt &
java ds.assignment3.PaxosNode M3 IMMEDIATE ACCEPTOR 4567 9 >./logs/m3.txt &
java ds.assignment3.PaxosNode M4 IMMEDIATE ACCEPTOR 4567 9 >./logs/m4.txt &
java ds.assignment3.PaxosNode M5 IMMEDIATE ACCEPTOR 4567 9 >./logs/m5.txt &
java ds.assignment3.PaxosNode M6 IMMEDIATE ACCEPTOR 4567 9 >./logs/m6.txt &
java ds.assignment3.PaxosNode M7 IMMEDIATE ACCEPTOR 4567 9 >./logs/m7.txt &
java ds.assignment3.PaxosNode M8 IMMEDIATE ACCEPTOR 4567 9 >./logs/m8.txt &
java ds.assignment3.PaxosNode M9 IMMEDIATE ACCEPTOR 4567 9 >./logs/m9.txt &

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