/** 
* PaxosNode.java
* This file implements the a Paxos node (can be both proposer and acceptor)
*/

package ds.assignment3;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;

public class PaxosNode {

    private String name;                // name of node
    private String profile;             // behaviour of node, one of immediate, medium, late or never
    private int n_nodes;                // number of nodes in the network
    private Socket socket;              // active socket
    private String port;                // local port

    private int propose_id;             // propose id
    private int max_id;                 // maximum id promised
    private Boolean accepted;           // whether accepted a value
    private int accepted_id;            // id accepted
    private String accepted_val;        // value accepted
    private long starttime;             // start timestamp

    /**
    * constructor
    */
    public PaxosNode(String name, String profile, int n_nodes, Socket socket) {
        // store node information
        this.name = name;
        this.profile = profile;
        this.n_nodes = n_nodes;
        this.socket = socket;
        this.port = Integer.toString(socket.getLocalPort());

        // initialise Paxos parameters
        propose_id = 0;
        max_id = 0;
        accepted = false;
        accepted_id = 0;
        accepted_val = null;
        starttime = System.currentTimeMillis();
    }

    /**
    * Print line to terminal with timestamp
    */
    public void printlnTime(String msg) {
        PaxosHelper.printlnTime("[Node " + name + "] " + msg);
    }

    /**
    * Print error message to terminal with timestamp
    */
    public void printErrTime(String msg) {
        PaxosHelper.printlnTime("[Node " + name + "] Error: " + msg);
    }

    /**
    * Generate a random integer between min and max
    *
    * @param min minimum range
    * @param min maximum range
    */
    public int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
    * Run the proposer's functions
    */
    void proposer() {
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            printErrTime("Sleep interrupted");
        }

        try {
            // create input and output buffer
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(socket.getOutputStream());

            // send prepare message until received from a majority of acceptors
            List<String> promises = new ArrayList<>();
            String line = "";
            int received_count;
            while(line != null) {
                // propose id uses current timestamp for unique and incrementing
                propose_id = (int) (System.currentTimeMillis() - starttime);
                output.println(port + " PREPARE " + Integer.toString(propose_id));
                printlnTime("Prepare ID: " + Integer.toString(propose_id));

                // clear list and counter
                promises.clear();
                received_count = 0;

                // wait for response from acceptors
                while(true) {
                    try {
                        line = input.readLine();
                        if(line != null) {
                            printlnTime("Received: " + line);

                            String[] parts = line.split(" ");
                            String func = parts[1];

                            if (func.equals("PROMISE")) {
                                // promise if id match
                                if(parts[2].equals(Integer.toString(propose_id))) {
                                    promises.add(line);
                                    received_count ++;
                                }
                            } else if(func.equals("CONSENSUS")) {
                                // consensus reached
                                printlnTime("Consensus reached on value: " + parts[3]);
                                return;
                            }
                            
                        } else {
                            printErrTime("Connection closed");
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        printlnTime("Read timeout");
                        break;
                    }

                    if(received_count > (n_nodes / 2)) break;
                }

                if(received_count > (n_nodes / 2)) {
                    // received from a majority of acceptors
                    printlnTime("Received promises from majority of nodes");

                    // check if any responses contain accepted values from other proposals
                    String propose_val = name;
                    for(String promise : promises) {
                        // format: PORT PROMISE id | PORT PROMISE id accepted_id accepted_val
                        String[] parts = promise.split(" ");

                        if (parts.length == 5) {
                            // contains accepted value
                            int aid = Integer.parseInt(parts[3]);
                            String aval = parts[4];
                            if(aid > accepted_id) {
                                // value from PROMISE message with the highest accepted ID
                                accepted_id = aid;
                                propose_val = aval;
                            }
                        }
                    }

                    // delay response according to profile
                    try {
                        if (profile.equals("MEDIUM")) {
                            // Respond after 2s, 20% probability go offline
                            Thread.sleep(2000);
                            if(randInt(1,100) <= 20) {
                                printlnTime("Gone offline");
                                return;
                            }
                        } else if (profile.equals("LATE")) {
                            // Respond after 5s, 50% probability go offline
                            Thread.sleep(5000);
                            if(randInt(1,100) <= 50) {
                                printlnTime("Gone offline");
                                return;
                            }
                        } else if (profile.equals("NEVER")) {
                            return;
                        }
                    } catch (InterruptedException e) {
                        printErrTime("Sleep interrupted");
                    } 

                    // send propose message
                    output.println(port + " PROPOSE " + Integer.toString(propose_id) + " " + propose_val);
                    printlnTime("Propose value: " + propose_val);

                    // wait for response from acceptors
                    received_count = 0;
                    while(true) {
                        try {
                            line = input.readLine();
                            if(line != null) {
                                printlnTime("Received: " + line);
                                
                                String[] parts = line.split(" ");
                                String func = parts[1];

                                if (func.equals("ACCEPT")) {
                                    // accept if id match
                                    if(parts[2].equals(Integer.toString(propose_id))) received_count++;
                                } else if(func.equals("CONSENSUS")) {
                                    // consensus reached
                                    printlnTime("Consensus reached on value: " + parts[3]);
                                    return;
                                }

                                received_count ++;
                            } else {
                                printErrTime("Connection closed");
                                break;
                            }
                        } catch (SocketTimeoutException e) {
                            printlnTime("Read timeout");
                            break;
                        }

                        if(received_count > (n_nodes / 2)) break;
                    }

                    if(received_count > (n_nodes / 2)) {
                        // received from a majority of acceptors
                        printlnTime("Consensus reached on value: " + propose_val);
                        break;
                    } else {
                        // propose failed
                        printlnTime("No concensus reached in this Paxos round");
                    }
                }
            }

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            printErrTime("Connection closed");
        }  
    }

    /**
    * Run the acceptor's functions
    */
    void acceptor() {
        try {
            // create input and output buffer
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(socket.getOutputStream());

            // read for prepare message
            String line = "";
            while(true) {
                try {
                    line = input.readLine();

                    // delay response according to profile
                    try {
                        if (profile.equals("MEDIUM")) {
                            // Respond after 2s, 20% probability lose of packet
                            Thread.sleep(2000);
                            if(randInt(1,100) <= 20) {
                                printlnTime("Packet Lost");
                                continue;
                            }
                        } else if (profile.equals("LATE")) {
                            // Respond after 5s, 50% probability lose of packet
                            Thread.sleep(5000);
                            if(randInt(1,100) <= 50) {
                                printlnTime("Packet Lost");
                                continue;
                            }
                        } else if (profile.equals("NEVER")) {
                            // Never respond
                            continue;
                        }
                    } catch (InterruptedException e) {
                        printErrTime("Sleep interrupted");
                    }

                    if(line != null) {
                        printlnTime("Received: " + line);

                        // parse message
                        // format [PORT] [FUNC] [ARGS]
                        String[] parts = line.split(" ");
                        String port = parts[0];
                        String func = parts[1];
                        int id = Integer.parseInt(parts[2]);

                        if(func.equals("PREPARE")) {
                            // prepare message
                            if(id > max_id) {
                                // promise to prepare message
                                max_id = id;
                                if(accepted) {
                                    // respond: PROMISE(ID, accepted_ID, accepted_VALUE)
                                    output.println(port + " PROMISE " + parts[2] + " " + Integer.toString(accepted_id) + " " + accepted_val);
                                } else {
                                    // respond: PROMISE(ID)
                                    output.println(port + " PROMISE " + parts[2]);
                                }
                                printlnTime("Promise sent");
                            }
                        } else if(func.equals("PROPOSE")) {
                            // propose message
                            if(id == max_id) {
                                // largest id so far
                                // respond: ACCEPTED(ID, VALUE) to the proposer
                                accepted = true;
                                accepted_id = id;
                                accepted_val = parts[3];
                                output.println(port + " ACCEPT " + parts[2] + " " + parts[3]);
                                printlnTime("Accept sent");
                            }
                        } else if (func.equals("CONSENSUS")) {
                            // consensus notice from learner
                            printlnTime("Consensus reached on value: " + parts[3]);
                            break;
                        } else {
                            // unknown command
                        }

                    } else {
                        printErrTime("Connection closed");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    printlnTime("Read timeout");
                }
            }

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            printErrTime("Connection closed");
        }
    }

    /**
    * Run the acceptor's functions
    */
    void learner() {
        try {
            // create input and output buffer
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(socket.getOutputStream());

            // map to store the accept count of IDs
            // <ID, count>
            HashMap<String,Integer> accept_count = new HashMap<String,Integer>();

            // read for prepare message
            String line = "";
            while(true) {
                try {
                    line = input.readLine();

                    // delay response according to profile
                    try {
                        if (profile.equals("MEDIUM")) {
                            // Respond after 2s, 20% probability lose of packet
                            Thread.sleep(2000);
                            if(randInt(1,100) <= 20) {
                                printlnTime("Packet Lost");
                                continue;
                            }
                        } else if (profile.equals("LATE")) {
                            // Respond after 5s, 50% probability lose of packet
                            Thread.sleep(5000);
                            if(randInt(1,100) <= 50) {
                                printlnTime("Packet Lost");
                                continue;
                            }
                        } else if (profile.equals("NEVER")) {
                            // Never respond
                            continue;
                        }
                    } catch (InterruptedException e) {
                        printErrTime("Sleep interrupted");
                    }

                    if(line != null) {
                        // parse message
                        // format: [PORT] [FUNC] [ARGS]
                        String[] parts = line.split(" ");

                        String func = parts[1];
                        String id = parts[2];

                        if(func.equals("ACCEPT")) {
                            // accept message
                            printlnTime("Received: " + line);
                            
                            if(accept_count.containsKey(id)) {
                                accept_count.put(id,accept_count.get(id) + 1);
                            } else {
                                accept_count.put(id,1);
                            }

                            if(accept_count.get(id) > (n_nodes / 2)) {
                                output.println("0 CONSENSUS " + id + " " + parts[3]);
                                printlnTime("Consensus reached on value: " + parts[3]);
                                break;
                            }
                        } else {
                            // unknown command
                        }

                    } else {
                        printErrTime("Connection closed");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    printlnTime("Read timeout");
                }
            }

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            printErrTime("Connection closed");
        }
    }

    /**
    * Main function for the Paxos node
    */
    public static void main(String args[]) {
        // check input arguments
        if (args.length < 5) {
            System.err.println("Usage: [name] [profile] [role] [port] [n_nodes]");
            System.exit(0);
        }

        String name = args[0];
        String profile = args[1];
        String role = args[2];
        int port = Integer.parseInt(args[3]);
        int n_nodes = Integer.parseInt(args[4]);

        try {
            // create connection with server
            Socket socket = new Socket("127.0.0.1", port);

            // timeout after 10s
            socket.setSoTimeout(10000);

            // initialise node
            PaxosNode node = new PaxosNode(name, profile, n_nodes, socket);
            node.printlnTime("Initialised at port " + node.port);

            // act according to role
            if (role.equals("PROPOSER")) {
                // node is proposer
                node.proposer();
            } else if (role.equals("ACCEPTOR")) {
                // node is acceptor
                node.acceptor();
            } else if (role.equals("LEARNER")) {
                // node is learner
                node.learner();
            } else {
                // unknown role
            }
        } catch (IOException e) {
            PaxosHelper.printErrTime("Failed to connect to communication manager");
        }
    }
}