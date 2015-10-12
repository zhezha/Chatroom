package chatroom;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Server object performs all server functionalities and implements ServerInterface
 * @author Zhao Zhengyang
 */
public class ChatroomServer extends UnicastRemoteObject implements ServerInterface {
    
    private static final long serialVersionUID = 1L;

    /**An instance of RegistrationCenter*/
    private RegistrationCenter registrationCenter = new RegistrationCenter();
    
    /**
     * A table of current chatroom names and Chatroom objects
     * key: chatroom name, value: chatroom object
     */
    private Hashtable<String, Chatroom> chatroomTable = new Hashtable<>();
    
    /**
     * A table records every client's position
     * key: client name, value: chatroom client is visiting
     */
    private Hashtable<String, String> clientPositionTable = new Hashtable<>();

    /**Constructor, create an instance of ChatroomServer*/
    public ChatroomServer() throws RemoteException, MalformedURLException {

        super();

        try {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(1099);
        }
        //set rmi address *****************************************************
        Naming.rebind("rmi://localhost:1099/chatroom", this);

        System.out.println("ChatroomServer is ready!");
    }

    /**
     * Add clientName to clientPositionTable
     * @param clientName 
     */
    public void clientStart(String clientName) {
        clientPositionTable.put(clientName, "");
    }

    /**
     * Remove client from clientPositionTable
     * Inform relevant clients about the disappearance of chatrooms created by the client
     * @param clientName
     * @param clientInterface
     * @throws RemoteException 
     */
    public void clientQuit(String clientName, ClientInterface clientInterface) throws RemoteException {

        //before quit, exit the visiting chatroom first.
        if (clientPositionTable.get(clientName).length() > 0) {
            String chatroom = clientPositionTable.get(clientName);
            exitChatroom(chatroom, clientName);
        }
        clientPositionTable.remove(clientName);
        //get the list of rooms created by the quit client and delete the rooms from chatroomTable
        ArrayList<String> list = registrationCenter.processQuitClient(clientName, clientInterface);
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //inform every participant of the chatroom about the chatroom elimination
                Hashtable<String, ClientInterface> participantTable = chatroomTable.get(list.get(i)).getParticipantTable();
                Iterator it = participantTable.keySet().iterator();
                while (it.hasNext()) {
                    String participantName = (String) it.next();
                    clientPositionTable.put(participantName, "");
                    participantTable.get(participantName).handleChatroomElimination();
                }

                chatroomTable.remove(list.get(i));
            }
        }
    }

    /**
     * Client invokes this method to get current existing chatrooms
     * @return chatroomList chatroom names and creators
     */
    public synchronized ArrayList<String[]> getChatroomList() {

        ArrayList<String[]> chatroomList = new ArrayList<>();
        Iterator it = chatroomTable.keySet().iterator();

        while (it.hasNext()) {
            String[] chatroomItem = new String[2];
            chatroomItem[0] = (String) it.next();
            chatroomItem[1] = chatroomTable.get(chatroomItem[0]).getCreator();
            chatroomList.add(chatroomItem);
        }
        return chatroomList;
    }

    /**
     * Client subscribes to the registration center
     * @param clientInterface 
     */
    public synchronized void subscribe(ClientInterface clientInterface) {
        registrationCenter.subscribe(clientInterface);
    }

    /**
     * Client unsubscribes to the registration center
     * @param clientInterface 
     */
    public synchronized void unsubscribe(ClientInterface clientInterface) {
        registrationCenter.unsubscribe(clientInterface);
    }

    /**
     * Create a new chatroom
     * @param chatroomName
     * @param clientName
     * @throws RemoteException 
     */
    public synchronized void createChatroom(String chatroomName, String clientName) throws RemoteException {

        Chatroom chatroom = new Chatroom(chatroomName, clientName);
        chatroomTable.put(chatroomName, chatroom);
        registrationCenter.registerChatroom(chatroomName, clientName);

    }

    /**
     * Destroy a chatroom
     * @param chatroomName
     * @throws RemoteException 
     */
    public synchronized void destroyChatroom(String chatroomName) throws RemoteException {

        Hashtable<String, ClientInterface> participantTable = chatroomTable.get(chatroomName).getParticipantTable();
        Iterator it = participantTable.keySet().iterator();
        while (it.hasNext()) {
            String participantName = (String) it.next();
            clientPositionTable.put(participantName, "");
            participantTable.get(participantName).handleChatroomElimination();
        }

        chatroomTable.remove(chatroomName);
        registrationCenter.unregisterChatroom(chatroomName);
    }

    /**
     * Enter a chatroom
     * @param chatroomName
     * @param clientName
     * @param clientInterface
     * @return list participant list of the chatroom
     * @throws RemoteException 
     */
    public synchronized ArrayList<String> enterChatroom(String chatroomName, String clientName, ClientInterface clientInterface) throws RemoteException {

        ArrayList<String> list = new ArrayList<>();
        
        if (chatroomTable.containsKey(chatroomName)) {
            DateFormat df = DateFormat.getDateTimeInstance();
            String time = df.format(new Date());
            String msg = time + "  " + clientName + " enters the room.";
            chatroomTable.get(chatroomName).sendToAll(msg);

            chatroomTable.get(chatroomName).addParticipant(clientName, clientInterface);
            list = chatroomTable.get(chatroomName).getParticipantList();

            clientPositionTable.put(clientName, chatroomName);
        }
        //if chatroom not exist, return ArrayList with a single str "not exist".
        else {
            String str = "not exist";
            list.add(str);
        }
        return list;
    }

    /**
     * Exit a chatroom
     * @param chatroomName
     * @param clientName
     * @throws RemoteException 
     */
    public synchronized void exitChatroom(String chatroomName, String clientName) throws RemoteException {

        chatroomTable.get(chatroomName).deleteParticipant(clientName);
        clientPositionTable.put(clientName, "");

        DateFormat df = DateFormat.getDateTimeInstance();
        String time = df.format(new Date());
        String msg = time + "  " + clientName + " leaves the room.";
        chatroomTable.get(chatroomName).sendToAll(msg);
    }

    /**
     * Refresh the chatroom participants list
     * @param chatroomName
     * @return list participants list of the chatroom
     */
    public synchronized ArrayList<String> refreshParticipantsList(String chatroomName) {

        ArrayList<String> list = chatroomTable.get(chatroomName).getParticipantList();
        return list;
    }

    /**
     * Client invokes this method to send message
     * @param chatroomName name of the chatroom client is participating
     * @param sender
     * @param receiver
     * @param msg
     * @throws RemoteException 
     */
    public synchronized void sendMsg(String chatroomName, String sender, String receiver, String msg) throws RemoteException {

        DateFormat df = DateFormat.getDateTimeInstance();
        String time = df.format(new Date());

        Chatroom chatroom = chatroomTable.get(chatroomName);
        String str = time + "\n";
        if (receiver.length() == 0) {
            str = str + sender + " to all: " + msg;
            chatroom.sendToAll(str);
        } 
        else {
            str = str + sender + " to " + receiver + ": " + msg;
            chatroom.sendToSingle(str, sender, receiver);
        }
    }

    /**
     * Search a user's current position
     * @param name
     * @return result chatroom the user under searching is visiting
     */
    public String SearchPerson(String name) {
        String result;
        if (clientPositionTable.containsKey(name)) {
            result = clientPositionTable.get(name);
        }
        else {
            result = "not exist";
        }
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException, MalformedURLException {
        // TODO code application logic here

        ChatroomServer server = new ChatroomServer();

    }

}
