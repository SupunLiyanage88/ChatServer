# ChatServer

A Java-based chat application with client-server architecture that allows multiple users to communicate in real-time through a graphical user interface.

## Features

- Multi-client chat system with a central server
- Private messaging support between users
- Real-time user list updates
- Both broadcast and targeted messaging
- Thread-safe server implementation
- Graphical user interface built with Java Swing

## Architecture

The application is divided into two main components:

### ChatServer

The server component handles:
- Client connections on port 9001
- User registration and name validation
- Message routing (both broadcast and private)
- Maintaining the list of connected users
- Thread safety for concurrent client handling

### ChatClient

The client component provides:
- A graphical interface built with Java Swing
- Connection to the chat server via IP address
- User list display showing all connected users
- Options for broadcast or private messaging
- Real-time message updates

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Java Runtime Environment (JRE) for running the compiled application

### Running the Server

1. Compile the server:
```bash
javac chatserver/ChatServer.java
```

2. Run the server:
```bash
java chatserver.ChatServer
```

3. The server will start and listen for connections on port 9001.

### Running the Client

1. Compile the client:
```bash
javac chatserver/ChatClient.java
```

2. Run the client:
```bash
java chatserver.ChatClient
```

3. Enter the server's IP address when prompted
4. Choose a unique username
5. Start chatting!

## Usage Guide

### Connecting to the Chat

1. Start the client application
2. Enter the server's IP address (use "localhost" for local testing)
3. Choose a unique username that isn't already in use

### Sending Messages

**Broadcast Messages:**
1. Check the "Broadcast" checkbox
2. Type your message
3. Click "Send" or press Enter

**Private Messages:**
1. Uncheck the "Broadcast" checkbox
2. Select one or more users from the online users list
3. Type your message
4. Click "Send" or press Enter

## Protocol

The client and server communicate using a simple text-based protocol:

- **SUBMITNAME**: Server requests a username
- **NAMEACCEPTED**: Server confirms username is accepted
- **MESSAGE**: Prefix for chat messages
- **USERLIST**: Updates the list of online users
- **USERJOINED**: Notifies when a new user joins
- **USERLEFT**: Notifies when a user leaves
- **BROADCAST**: Client signals a message should go to all users
- **PRIVATE**: Client signals a message targeted to specific users

## Implementation Details

### Thread Safety

The server uses synchronized blocks to ensure thread safety when:
- Managing the set of user names
- Broadcasting messages to clients
- Updating the user list

### Error Handling

- The server handles unexpected client disconnections
- The client provides feedback when users attempt invalid operations

## Future Improvements

- Add message timestamps
- Implement user authentication
- Add file sharing capabilities
- Support for chat rooms/channels
- Message history persistence
- Emoji support

## License

This project is available under the MIT License - see the LICENSE file for details.

## Acknowledgments

This chat application was created as a learning project for network programming and multi-threading in Java.
