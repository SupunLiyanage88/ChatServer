package chatserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */

public class ChatClient {
    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(30);
    JButton sendButton = new JButton("Send");
    JTextArea messageArea = new JTextArea(8, 40);
    
    // TODO: Add a list box

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */

    // get the online users
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> onlineList = new JList<>(listModel);
    JCheckBox checkbox = new JCheckBox("Broadcast");

    private static HashSet<String> selectedNames = new HashSet<>();

    public ChatClient() {
    	
        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
    	
        // config JList
        onlineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        onlineList.setVisibleRowCount(10);
        JScrollPane listScrollPane = new JScrollPane(onlineList);

        // frame layout
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 400);
        frame.setLayout(new BorderLayout());

        // online user panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Online Users"), BorderLayout.NORTH);
        rightPanel.add(listScrollPane, BorderLayout.CENTER);
        rightPanel.add(checkbox, BorderLayout.SOUTH);

        // message input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // components
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);

        
        textField.setEditable(false);
        messageArea.setEditable(false);

        frame.setVisible(true);

        // send button
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            if (checkbox.isSelected()) {
                // broadcast
                out.println("BROADCAST " + message);
            } else {
                // selected users to send
                String selectedUsers = String.join(",", onlineList.getSelectedValuesList());
                if (!selectedUsers.isEmpty()) {
                    out.println("PRIVATE " + selectedUsers + " " + message);
                } else {
                    JOptionPane.showMessageDialog(frame, "Select at least one user or use Broadcast.");
                    return;
                }
            }
            textField.setText("");
            textField.requestFocus(); 
        }
    }

    //add user to list
    public void addUser(String username) {
        if (!listModel.contains(username)) {
            listModel.addElement(username);
        }
    }

    // remove user from list
    public void removeUser(String username) {
        listModel.removeElement(username);
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to the Chatter", JOptionPane.QUESTION_MESSAGE);
    }


    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {
    	// Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        String username = null; // store username
        
        // Process all messages from server, according to the protocol.
        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                username = getName();
                out.println(username);
            } else if (line.startsWith("NAMEACCEPTED")) {
                frame.setTitle("Chatter - " + username); // username in window title
                textField.setEditable(true);
                sendButton.setEnabled(true); 
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("USERLIST")) {
                listModel.clear();
                String[] users = line.substring(9).split(",");
                for (String user : users) {
                    addUser(user);
                }
            } else if (line.startsWith("USERJOINED")) {
                addUser(line.substring(11));
            } else if (line.startsWith("USERLEFT")) {
                String leftUser = line.substring(9);
                messageArea.append(leftUser + " has left the chat.\n"); 
                removeUser(leftUser); 
            }
            
        }
    }
    
    /**
     * Runs the client as an application with a closeable frame.
     */

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setVisible(true);
        client.run();
    }
}
