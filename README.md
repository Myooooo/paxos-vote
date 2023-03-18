# paxos-vote
A simple implementation of the Paxos concensus and voting protocol

## problem description
Welcome to the Suburbs Council Election!
This year, Suburbs Council is holding elections for council president. Any member of its nine person council is eligible to become council president.

Member M1 – M1 has wanted to be council president for a very long time. M1 is very chatty over social media and responds to emails/texts/calls almost instantly. It is as if M1 has an in-brain connection with their mobile phone!

Member M2 – M2 has also wanted to be council president for a very long time, except their very long time is longer than everybody else's. M2 lives in a remote part of the Suburbs and thus their internet connection is really poor, almost non-existent. Responses to emails come in very late, and sometimes only to one of the emails in the email thread, so it is unclear whether M2 has read/understood them all. However, M2 sometimes likes to work at Café @ Bottom of the Hill. When that happens, their responses are instant and M2 replies to all emails.

Member M3 – M3 has also wanted to be council president. M3 is not as responsive as M1, nor as late as M2, however sometimes emails completely do not get to M3. The other councilors suspect that it’s because sometimes M3 goes on retreats in the woods at the top of the Suburbs, completely disconnected from the world.

Members M4-M9 have no particular ambitions about council presidency and no particular preferences or animosities, so they will try to vote fairly. Their jobs keep them fairly busy and as such their response times  will vary.

How does voting happen: On the day of the vote, one of the councilors will send out an email/message to all councilors with a proposal for a president. A majority (half+1) is required for somebody to be elected president.

### Checklist
- Paxos implementation works when two councillors send voting proposals at the same time
- Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries
- Paxos implementation works when M1 – M9 have responses to voting queries suggested by the profiles above, including when M2 or M3 propose and then go offline
- BONUS: Paxos implementation works with a number ‘n’ of councilors with four profiles of response times: immediate;  medium; late; never

### Supported message formats
- [PREPARE ID]: From proposer to acceptors, prepare stage
- [PROMISE ID]: From acceptors to proposer, promise to proposer
- [PROMISE ID ACCEPTED_ID ACCEPTED_VALUE]: From acceptors to proposer, promise with accepted value
- [PROPOSE ID VALUE]: From proposer to acceptors, propose stage
- [ACCEPT ID VALUE]: From acceptors to proposer, accept value
- [CONSENSUS ID VALUE]: From learner to acceptors, notice them of reaching consensus

Program ends after all nodes processes terminate. 

### Profile definition
- [IMMEDIATE]: Respond immediately
- [MEDIUM]: Respond after 2s, 20% probability lose of packet
- [LATE]: Respond after 5s, 50% probability lose of packet
- [NEVER]: Never respond

### Compile the files
```bash
$javac -d ./ *.java
```

### Automated Tests
Test with 3 nodes: 1 proposer, 2 acceptors. 
All have immediate profile
```bash
$./test_simple_immediate.sh
```

Test with 9 nodes: 1 proposer, 8 acceptors. 
All have immediate profile
```bash
$./test_complex_immediate.sh
```

Test with 9 nodes: 2 proposer, 7 acceptors. 
All have immediate profile
```bash
$./test_two_proposer.sh
```

Test with 9 nodes: 3 proposers, 6 acceptors. 
Node profiles as specified in assignment instructions
```bash
$./test_complex_profile.sh
```
