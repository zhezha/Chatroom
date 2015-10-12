package chatroom;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Chatroom class 
 * Perform chatroom service
 * @author Zhao Zhengyang
 */
public class Chatroom implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**Chatroom name*/
    private String name;
    /**Chatroom creator*/
    private String creator;
    /**A list of chatroom participants*/
    private ArrayList<String> participantList = new ArrayList<String>();
    /**
     * A table of chatroom participants' names and their ClientInterface objects
     * key: participant name, value: participant clientInterface
     */
    private Hashtable<String, ClientInterface> participantTable = new Hashtable<>();
    
    /**Constructor, create Chatroom object*/
    public Chatroom(String name, String creator) {
        
        super();
        this.name = name;
        this.creator = creator;
    }
    
    /**
     * Get chatroom name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get chatroom creator
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Get chatroom participant list
     * @return participantList
     */
    public ArrayList<String> getParticipantList() {
        return participantList;
    }
    
    /**
     * Get chatroom participant table
     * @return participantTable
     */
    public Hashtable<String, ClientInterface> getParticipantTable() {
        return participantTable;
    }
    
    /**
     * Add participant to the chatroom
     * @param clientName
     * @param clientInterface 
     */
    public synchronized void addParticipant(String clientName, ClientInterface clientInterface) {
        this.participantList.add(clientName);
        this.participantTable.put(clientName, clientInterface);
    }
    
    /**
     * Delete participant from chatroom
     * @param clientName 
     */
    public synchronized void deleteParticipant(String clientName) {
        this.participantList.remove(clientName);
        this.participantTable.remove(clientName);
    }
    
    /**
     * Send msg to all participants in the chatroom
     * @param msg
     * @throws RemoteException 
     */
    public synchronized void sendToAll(String msg) throws RemoteException {
        
        for (int i = 0; i < participantList.size(); i++) {
            participantTable.get(participantList.get(i)).displayMsg(msg);
        }
    }
    
    /**
     * Send msg to spesific participant in the chatroom
     * @param msg
     * @param sender
     * @param receiver
     * @throws RemoteException 
     */
    public synchronized void sendToSingle(String msg, String sender, String receiver) throws RemoteException {
        
        if (participantList.contains(receiver)) {
            participantTable.get(sender).displayMsg(msg);
            participantTable.get(receiver).displayMsg(msg);
        }
        else {
            ClientInterface ci = participantTable.get(sender);
            String str = "participant not exist!";
            ci.showMsg(str);
        }
    }

}
