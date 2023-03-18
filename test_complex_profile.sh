#!/bin/bash
echo Testing with 9 nodes: 3 proposers, 6 acceptors
echo Node profile as specified in assignment instructions
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
java ds.assignment3.PaxosNode M2 LATE PROPOSER 4567 9 >./logs/m2.txt &
java ds.assignment3.PaxosNode M3 MEDIUM PROPOSER 4567 9 >./logs/m3.txt &
java ds.assignment3.PaxosNode M4 IMMEDIATE ACCEPTOR 4567 9 >./logs/m4.txt &
java ds.assignment3.PaxosNode M5 IMMEDIATE ACCEPTOR 4567 9 >./logs/m5.txt &
java ds.assignment3.PaxosNode M6 IMMEDIATE ACCEPTOR 4567 9 >./logs/m6.txt &
java ds.assignment3.PaxosNode M7 IMMEDIATE ACCEPTOR 4567 9 >./logs/m7.txt &
java ds.assignment3.PaxosNode M8 IMMEDIATE ACCEPTOR 4567 9 >./logs/m8.txt &
java ds.assignment3.PaxosNode M9 IMMEDIATE ACCEPTOR 4567 9 >./logs/m9.txt &

echo Running...

for job in `jobs -p`
do
    wait $job || let "FAIL+=1"
done
echo $FAIL

echo Paxos run finished
echo Showing learner log...
cat ./logs/l1.txt
echo

echo Showing proposer 1 log...
cat ./logs/m1.txt
echo

echo Showing proposer 2 log...
cat ./logs/m2.txt
echo

echo Showing proposer 3 log...
cat ./logs/m3.txt
echo

echo Results may vary significantly due to the randomness of profiles. Try running this test multiple times. 