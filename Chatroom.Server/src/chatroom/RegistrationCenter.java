package chatroom;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * RegistrationCenter class to provide registration ceter service
 * @author Zhao Zhengyang
 */
public class RegistrationCenter {

    /**
     * Client get this table to display current chatroom names and creators
     * key: chatroom name, value: chatroom creator
     */
    private Hashtable<String, String> chatroomListForUser = new Hashtable<>();
    /**A list of subscribed users' ClientInterface objects*/
    private ArrayList<ClientInterface> subscribedUsersList = new ArrayList<>();

    /**Creator, create RegistrationCenter object*/
    public RegistrationCenter() {

        super();
    }

    /**
     * Get chatroomListForUser
     * @return chatroomListForUser
     */
    public Hashtable<String, String> getChatroomTable() {
        return chatroomListForUser;
    }

    /**
     * Register a newly created chatroom
     * @param chatroomName
     * @param creator
     * @throws RemoteException 
     */
    public void registerChatroom(String chatroomName, String creator) throws RemoteException {

        this.chatroomListForUser.put(chatroomName, creator);
        String msg = "A new room is created, please refresh chatroom table.";
        for (int i = 0; i < subscribedUsersList.size(); i++) {
            subscribedUsersList.get(i).showMsg(msg);
        }
    }

    /**
     * Unregister a newly disappeared chatroom
     * @param chatroomName
     * @throws RemoteException 
     */
    public void unregisterChatroom(String chatroomName) throws RemoteException {

        this.chatroomListForUser.remove(chatroomName);
        String msg = "A room has been destroyed, please refresh chatroom table.";
        for (int i = 0; i < subscribedUsersList.size(); i++) {
            subscribedUsersList.get(i).showMsg(msg);
        }
    }

    /**
     * Subscribe a new client
     * @param clientInterface 
     */
    public void subscribe(ClientInterface clientInterface) {
        this.subscribedUsersList.add(clientInterface);
    }

    /**
     * Unsubscribe a client
     * @param clientInterface 
     */
    public void unsubscribe(ClientInterface clientInterface) {
        this.subscribedUsersList.remove(clientInterface);
    }

    /**
     * When a client quits the system, delete the chatrooms 
     * created by the quit client and return the list of chatrooms 
     * created by the quit client.
     * @param clientName
     * @param clientInterface
     * @return list a list of chatrooms created by the quit client
     * @throws RemoteException 
     */
    public ArrayList<String> processQuitClient(String clientName, ClientInterface clientInterface) throws RemoteException {

        ArrayList<String> list = new ArrayList<>();
        Iterator it = chatroomListForUser.keySet().iterator();
        while (it.hasNext()) {
            String chatroomName = (String) it.next();
            if (chatroomListForUser.get(chatroomName).equalsIgnoreCase(clientName)) {
                list.add(chatroomName);
            }
        }

        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                chatroomListForUser.remove(list.get(i));
            }
            String msg = "A room creator goes off line, please refresh chatroom table!";
            for (int i = 0; i < subscribedUsersList.size(); i++) {
                subscribedUsersList.get(i).showMsg(msg);
            }
        }
        
        if (subscribedUsersList.contains(clientInterface)) {
            subscribedUsersList.remove(clientInterface);
        }
        return list;
    }

}
