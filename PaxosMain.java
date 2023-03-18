/** 
* PaxosMain.java
* This file implements the main communication manager of the Paxos Algorithm
* which initialises the nodes
*/

package ds.assignment3;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ds.assignment3.PaxosHelper;

public class PaxosMain {
    /**
    * Main function for the Paxos algorithm
    */
    public static void main(String args[]) {
        // check input arguments
        if (args.length < 2) {
            System.err.println("Usage: [port] [n_nodes]");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);
        int n_nodes = Integer.parseInt(args[1]);

        // create a thread pool of size n_nodes to handle income requests
        ExecutorService executer = Executors.newFixedThreadPool(n_nodes + 1);
        List<Socket> sockets = new ArrayList<>();

        try {
            // initialise server socket
            ServerSocket serverSocket = new ServerSocket(port);
            int count = 0;

            PaxosHelper.printlnTime("Initialised server at port " + Integer.toString(port));

            // wait for all nodes to connect
            while (count < n_nodes + 1) {
                try {
                    // accept connection and store socket in list
                    Socket clientSocket = serverSocket.accept();
                    sockets.add(clientSocket);
                    PaxosHelper.printlnTime("Added node at port " + Integer.toString(clientSocket.getPort()));
                } catch(IOException e) {
                    PaxosHelper.printErrTime("Failed to accept connection");
                }
                count ++;
            }

            // start threads to listen on sockets
            for (Socket s : sockets) {
                RequestHandler handler = new RequestHandler(s, sockets);
                executer.execute(handler);
            }

            // wait for all threads to terminate
            executer.shutdown();
            try {
                executer.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                PaxosHelper.printErrTime("Interrupted");
            }

            serverSocket.close();

        } catch (IOException e) {
            PaxosHelper.printErrTime("Failed to create server socket");
        }
    }
}