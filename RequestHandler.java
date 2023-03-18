/** 
* RequestHandler.java
* This file implements the request handler thread for communication manager
*/

package ds.assignment3;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

import ds.assignment3.PaxosHelper;

public class RequestHandler implements Runnable {

    private Socket socket;          // the socket to listen on
    private List<Socket> sockets;   // sockets for all nodes

    /**
    * constructor
    */
    public RequestHandler(Socket socket, List<Socket> sockets) {
        // store active socket
        this.socket = socket;
        this.sockets = sockets;
    }

    /**
    * Forward message to node on port
    *
    * @param msg message to be forwarded
    * @param port port to be forwarded to
    */
    public void fwd(String msg, int port) {
        for(Socket s : sockets) {
            if(s.getPort() == port) {
                synchronized(s) {
                    try {
                        PrintStream output = new PrintStream(s.getOutputStream());
                        output.println(msg);
                    } catch (IOException e) {
                        //PaxosHelper.printErrTime("Forward failed");
                    }
                }
                break;
            }
        }
        PaxosHelper.printlnTime("Forwarded: " + msg);
    }

    /**
    * Forward message to all other nodes
    *
    * @param msg message to be forwarded
    */
    public void fwdAll(String msg) {
        for(Socket s : sockets) {
            if(s != socket) {
                synchronized(s) {
                    try {
                        PrintStream output = new PrintStream(s.getOutputStream());
                        output.println(msg);
                    } catch (IOException e) {
                        //PaxosHelper.printErrTime("Forward failed");
                    }
                }
            }
        }
        PaxosHelper.printlnTime("Forwarded to all: " + msg);
    }

    /**
    * Forward message to learner
    *
    * @param msg message to be forwarded
    */
    public void fwdLearner(String msg) {
        Socket learner = sockets.get(0);
        try {
            synchronized(learner) {
                PrintStream output = new PrintStream(sockets.get(0).getOutputStream());
                output.println(msg);
            }
        } catch (IOException e) {
            //PaxosHelper.printErrTime("Forward failed");
        }
        PaxosHelper.printlnTime("Forwarded to learner: " + msg);
    }

    /**
    * Override of Thread execution method
    */
    @Override
    public void run() {

        PaxosHelper.printlnTime("Starting thread for node at port " + Integer.toString(socket.getPort()));
        
        try {
            // create input and output buffer
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // read from client
            String line = input.readLine();

            while(line != null) {
                PaxosHelper.printlnTime("Received: " + line);

                // parse incoming message
                // Format: [Port] [Function] [Arguments] 
                String[] parts = line.split(" ");
                int port = Integer.parseInt(parts[0]);
                String func = parts[1];
                //String msg = line.substring(line.indexOf(" ") + 1);

                if(func.equals("PREPARE")) {
                    // Prepare message from proposer, forward to all other nodes
                    fwdAll(line);
                } else if(func.equals("PROPOSE")) {
                    // Propose message from proposer, forward to all other nodes
                    fwdAll(line);
                } else if(func.equals("PROMISE")) {
                    // Propose message from a acceptor, forward to proposer
                    fwd(line, port);
                } else if(func.equals("ACCEPT")) {
                    // Accept message from a acceptor, forward to proposer and learner
                    fwd(line, port);
                    fwdLearner(line);
                } else if(func.equals("CONSENSUS")) {
                    // Consensus message from a learner, forward to all other nodes
                    fwdAll(line);
                } else {
                    // unknown command
                }

                // read from socket
                line = input.readLine();
            }

            input.close();
            socket.close();
        } catch (IOException e) {
            PaxosHelper.printErrTime("Failed to create IO buffer");
        }
    }
}