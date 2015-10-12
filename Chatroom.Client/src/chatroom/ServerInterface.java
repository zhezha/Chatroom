package chatroom;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * rmi Interface of server
 * @author Zhao Zhengyang
 */
public interface ServerInterface extends Remote{
    /**
     * Add clientName to clientPositionTable
     * @param clientName
     * @throws RemoteException 
     */
    void clientStart(String clientName) throws RemoteException;
    /**
     * Remove client from clientPositionTable
     * Inform relevant clients about the disappearance of chatrooms created by the client
     * @param clientName
     * @param clientInterface
     * @throws RemoteException 
     */
    void clientQuit(String clientName, ClientInterface clientInterface) throws RemoteException;
    /**
     * Client invokes this method to get current chatrooms
     * @return chatroomList chatroom names and creators
     * @throws RemoteException 
     */
    ArrayList<String[]> getChatroomList() throws RemoteException;
    /**
     * Client subscribes to the registration center
     * @param clientInterface
     * @throws RemoteException 
     */
    void subscribe(ClientInterface clientInterface) throws RemoteException;
    /**
     * Client unsubscribes to the registration center
     * @param clientInterface
     * @throws RemoteException 
     */
    void unsubscribe(ClientInterface clientInterface) throws RemoteException;
    /**
     * Create a new chatroom
     * @param chatroomName
     * @param clientName
     * @throws RemoteException 
     */
    void createChatroom(String chatroomName, String clientName) throws RemoteException;
    /**
     * Destroy a chatroom
     * @param chatroomName
     * @throws RemoteException 
     */
    void destroyChatroom(String chatroomName) throws RemoteException;
    /**
     * Enter a chatroom
     * @param chatroomName
     * @param clientName
     * @param clientInterface
     * @return list participant list of the chatroom
     * @throws RemoteException 
     */
    ArrayList<String> enterChatroom(String chatroomName, String clientName, ClientInterface clientInterface) throws RemoteException;
    /**
     * Exit a chatroom
     * @param chatroomName
     * @param clientName
     * @throws RemoteException 
     */
    void exitChatroom(String chatroomName, String clientName) throws RemoteException;
    /**
     * Refresh the chatroom participants list
     * @param chatroomName
     * @return list participants list of the chatroom
     * @throws RemoteException 
     */
    ArrayList<String> refreshParticipantsList(String chatroomName) throws RemoteException;
    /**
     * Client invokes this method to send message
     * @param chatroomName name of the chatroom client is participating
     * @param sender
     * @param receiver
     * @param msg
     * @throws RemoteException 
     */
    void sendMsg(String chatroomName, String sender, String receiver, String msg) throws RemoteException;
    /**
     * 
     * @param name
     * @return
     * @throws RemoteException 
     */
    String SearchPerson(String name) throws RemoteException;
    
}
