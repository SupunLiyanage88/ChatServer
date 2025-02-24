package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */

public class ChatServer {
	
    /**
     * The port that the server listens on.
     */

    private static final int PORT = 9001;
    
    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */

    private static HashSet<String> names = new HashSet<>(); 
    
    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();
    
    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                Socket socket = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }
    
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */

        public Handler(Socket socket) {
            this.socket = socket;
        }
        
        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */

        public void run() {
            try {
            	// Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }

                    // TODO: Add code to ensure the thread safety of the
                    // the shared variable 'names'
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            clientWriters.put(name, out); 
                            break;
                        }
                    }
                }
                
                
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");

                // Notify all clients of the new user
                broadcastUserList();
                
                // TODO: You may have to add some code here to broadcast all clients the new
                // client's name for the task 9 on the lab sheet. 

                
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.

                // Listen for messages
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    
                    // TODO: Add code to send a message to a specific client and not
                    // all clients. You may have to use a HashMap to store the sockets along 
                    // with the chat client names
                    if (input.startsWith("BROADCAST ")) {
                        broadcastMessage(name, input.substring(10));
                    } else if (input.startsWith("PRIVATE ")) {
                        handlePrivateMessage(name, input.substring(8));
                    }
                }
                // TODO: Handle the SocketException here to handle a client closing the socket
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                cleanup();
            }
        }

        // update all users
        private void broadcastUserList() {
            String userListMessage = "USERLIST " + String.join(",", names);
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters.values()) {
                    writer.println(userListMessage);
                }
            }
        }

        // send broadcast message
        private void broadcastMessage(String sender, String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters.values()) {
                    writer.println("MESSAGE " + sender + ": " + message);
                }
            }
        }

        // send private messages
        private void handlePrivateMessage(String sender, String messageData) {
            int firstSpace = messageData.indexOf(" ");
            if (firstSpace == -1) return; 

            String recipientsStr = messageData.substring(0, firstSpace);
            String message = messageData.substring(firstSpace + 1);

            String[] recipients = recipientsStr.split(",");
            synchronized (clientWriters) {
                for (String recipient : recipients) {
                    PrintWriter writer = clientWriters.get(recipient);
                    if (writer != null) {
                        writer.println("MESSAGE (Private) " + sender + ": " + message);
                    }
                }
            }
        }

        // clean when client leave
        private void cleanup() {
            if (name != null) {
                synchronized (names) {
                    names.remove(name);
                }
                synchronized (clientWriters) {
                    clientWriters.remove(name);
                }
                
                
                broadcastMessage("Server", name + " has left the chat.");
                
                
                broadcastUserList();
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }
}
