package chatroom;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for server to perform callback.
 * @author Zhao Zhengyang
 */
public interface ClientInterface extends Remote{

    /**
     * Server calls this method of client to show notification
     * @param msg notification
     * @throws RemoteException 
     */
    void showMsg(String msg) throws RemoteException;
    /**
     * When currentChatroom disappears with its creator, 
     * server invokes this method to update the states of
     * corresponding participants.
     * @throws RemoteException 
     */
    void handleChatroomElimination() throws RemoteException;
    /**
     * Display msg on message display area
     * @param msg message
     * @throws RemoteException 
     */
    void displayMsg(String msg) throws RemoteException;
}
