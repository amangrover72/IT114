package server;	
import java.util.ArrayList;	
import java.util.Iterator;	
import java.util.List;	
import java.util.logging.Level;	
import java.util.logging.Logger;	
import java.util.Random;	
public class Room implements AutoCloseable {	
    private static SocketServer server; // used to refer to accessible server functions	
    private String name;	
    private final static Logger log = Logger.getLogger(Room.class.getName());	
    // Commands	
    private final static String COMMAND_TRIGGER = "/";	
    private final static String CREATE_ROOM = "createroom";	
    private final static String JOIN_ROOM = "joinroom";	
    private final static String ROLL = "roll";	
    private final static String FLIP = "flip";
    private final static String PM = "@"; 
    private final static String MUTE = "mute";
    private final static String UNMUTE = "unmute";

    public Room(String name) {	
        this.name = name;	
    }	
    public static void setServer(SocketServer server) {	
        Room.server = server;	
    }	
    public String getName() {	
        return name;	
    }	
    private List < ServerThread > clients = new ArrayList < ServerThread > ();	
    protected synchronized void addClient(ServerThread client) {	
        client.setCurrentRoom(this);	
        if (clients.indexOf(client) > -1) {	
            log.log(Level.INFO, "Attempting to add a client that already exists");	
        } else {	
            clients.add(client);	
            if (client.getClientName() != null) {	
                client.sendClearList();	
                sendConnectionStatus(client, true, "joined the room " + getName());	
                updateClientList(client);	
            }	
        }	
    }	
    private void updateClientList(ServerThread client) {	
        Iterator < ServerThread > iter = clients.iterator();	
        while (iter.hasNext()) {	
            ServerThread c = iter.next();	
            if (c != client) {	
                boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);	
            }	
        }	
    }	
    protected synchronized void removeClient(ServerThread client) {	
        clients.remove(client);	
        if (clients.size() > 0) {	
            // sendMessage(client, "left the room");	
            sendConnectionStatus(client, false, "left the room " + getName());	
        } else {	
            cleanupEmptyRoom();	
        }	
    }	
    private void cleanupEmptyRoom() {	
        // If name is null it's already been closed. And don't close the Lobby	
        if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {	
            return;	
        }	
        try {	
            log.log(Level.INFO, "Closing empty room: " + name);	
            close();	
        } catch (Exception e) {	
            // TODO Auto-generated catch block	
            e.printStackTrace();	
        }	
    }	
    protected void joinRoom(String room, ServerThread client) {	
        server.joinRoom(room, client);	
    }	
    protected void joinLobby(ServerThread client) {	
        server.joinLobby(client);	
    }	
    Random rand = new Random();	
    /***	
     * Helper function to process messages to trigger different functionality.	
     * 	
     * @param message The original message being sent	
     * @param client  The sender of the message (since they'll be the ones	
     *                triggering the actions)	
     */	
    private String processCommands(String message, ServerThread client) {	
        // boolean wasCommand = false;	
        String response = null;	
        try {	
            if (message.indexOf(COMMAND_TRIGGER) > -1) {	
                String[] comm = message.split(COMMAND_TRIGGER);	
                log.log(Level.INFO, message);	
                String part1 = comm[1];	
                String[] comm2 = part1.split(" ");	
                String command = comm2[0];	
                if (command != null) {	
                    command = command.toLowerCase();	
                }	
                String roomName;	
                switch (command) {	
                    case CREATE_ROOM:	
                        roomName = comm2[1];	
                        if (server.createNewRoom(roomName)) {	
                            joinRoom(roomName, client);	
                        }	
                        // wasCommand = true;	
                        break;	
                    case JOIN_ROOM:	
                        roomName = comm2[1];	
                        joinRoom(roomName, client);	
                        // wasCommand = true;	
                        break;	
                    case ROLL:	
					response = "<u><i><b style=\"color: blue;\">You rolled a " + Integer.toString(rand.nextInt(6) + 1) + "</b></i></u>";
                        // wasCommand = true;	
                        break;	
                    case FLIP:	
                        String[] coin = {	
                            "Heads",	
                            "Tails"	
                        };	
                        response = "<u><i><b style=\"color: blue;\">You got " + coin[rand.nextInt(coin.length)] + "</b></i></u>";	
                        // wasCommand = true;	
                        break;	
                    default:	
                        response = message;	
                        break;	

                     case MUTE:
                        String[] splitMsg = message.split(" ");
                        //message will be  /mute user
                        String mutedClient = splitMsg[1];
                        client.mutedList.add(mutedClient);
                        //sendMessage(client,"<u><i><b style=\"color: blue;\">muted "+mutedClient+"</b></i></u>");
                        //sends a message to the muted user and the client that muted them
		            	Iterator<ServerThread> iter = clients.iterator();
		            	while (iter.hasNext()) {
		        	    	ServerThread c = iter.next();
		        		        if (c.getClientName().equals(mutedClient)||c.getClientName().equals(client.getClientName())) {
		    		            	c.send(client.getClientName(),"<u><i><b style=\"color: blue;\">muted "+mutedClient+"</b></i></u>");
				                }
                        }
                        //wasCommand = true;
                        break;
                    case UNMUTE:
                        String[] splitArr = message.split(" ");
                        String unmutedClient = splitArr[1];
                        for(String name: client.mutedList) {
                            if(name.equals(unmutedClient)) {
                                client.mutedList.remove(unmutedClient);
                                //sendMessage(client,"<u><i><b style=\"color: blue;\">unmuted "+unmutedClient+"</b></i></u>");
                                //sends a message to the unmuted user and the client that unmuted them
					            Iterator<ServerThread> iter1 = clients.iterator();
				            	while (iter1.hasNext()) {
						           ServerThread c = iter1.next();
						              if (c.getClientName().equals(unmutedClient)||c.getClientName().equals(client.getClientName())) {
					            		c.send(client.getClientName(),"<u><i><b style=\"color: blue;\">unmuted "+unmutedClient+"</b></i></u>");
					                	}
					            }
                                //wasCommand = true;
                                break;
                            }   
                        }
                    break;
                }	
            } else {	
                // Bold	
                if (message.contains("**")) {	
                    message = message.replaceAll("\\*\\*\\b", "<b>").replaceAll("\\b\\*\\*", "</b>");	
                }	
                // Italics	
                if (message.contains("*")) {	
                    message = message.replaceAll("\\*\\b", "<i>").replaceAll("\\b\\*", "</i>");	
                }	
                // Underline	
                if (message.contains("__")) {	
                    message = message.replaceAll("\\b__", "<u>").replaceAll("__\\b", "</u>");	
                }	
                response = message;	
            }	
        } catch (Exception e) {	
            e.printStackTrace();	
        }	
        // return wasCommand;	
        return response;	
    }	
    // TODO changed from string to ServerThread	
    protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {	
        Iterator < ServerThread > iter = clients.iterator();	
        while (iter.hasNext()) {	
            ServerThread c = iter.next();	
            boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);	
            if (!messageSent) {	
                iter.remove();	
                log.log(Level.INFO, "Removed client " + c.getId());	
            }	
        }	
    }	
    /***	
     * Takes a sender and a message and broadcasts the message to all clients in	
     * this room. Client is mostly passed for command purposes but we can also use	
     * it to extract other client info.	
     * 	
     * @param sender  The client sending the message	
     * @param message The message to broadcast inside the room	
     */	
    protected void sendMessage(ServerThread sender, String message) {	
        log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");	
        String response = processCommands(message, sender);	
        if (response == null) {	
            // it was a command, don't broadcast	
            return;	
        }	
        if (sendPM(sender, message)){
            return;
        }

        message = response;	
        Iterator < ServerThread > iter = clients.iterator();	
        while (iter.hasNext()) {	
            ServerThread client = iter.next();
            if(!client.isMuted(sender.getClientName())) {
                boolean messageSent = client.send(sender.getClientName(), message);
                if (!messageSent) {
                iter.remove();
                log.log(Level.INFO, "Removed client " + client.getId());
                }
            }
        }
    }	

    //sends a private message if a specific user is tagged
    protected boolean sendPM(ServerThread sender, String message) {
    	boolean isPM = false;
    	String receiver = null;

    	if (message.indexOf("@") > -1) {
			String[] words = message.split(" ");
			for(String word: words){
			    if (word.charAt(0)=='@'){
			        receiver = word.substring(1);
			        isPM = true;
			        //now that the message is known to be private, we can send it to each receiver

			        Iterator<ServerThread> iter = clients.iterator();
					while (iter.hasNext()) {
						ServerThread c = iter.next();
						if (c.getClientName().equals(receiver)&&(!c.isMuted(sender.getClientName()))) {
							c.send(sender.getClientName(), message);
						}
					}
			    }
			}
			//send one message to the sender so they can see it went through
			sender.send(sender.getClientName(), message);
		}
    	//return true boolean
    	return isPM;
    }
    
    /***	
     * Will attempt to migrate any remaining clients to the Lobby room. Will then	
     * set references to null and should be eligible for garbage collection	
     */	
    @Override	
    public void close() throws Exception {	
        int clientCount = clients.size();	
        if (clientCount > 0) {	
            log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");	
            Iterator < ServerThread > iter = clients.iterator();	
            Room lobby = server.getLobby();	
            while (iter.hasNext()) {	
                ServerThread client = iter.next();	
                lobby.addClient(client);	
                iter.remove();	
            }	
            log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");	
        }	
        server.cleanupRoom(this);	
        name = null;	
        // should be eligible for garbage collection now	
    }	
}