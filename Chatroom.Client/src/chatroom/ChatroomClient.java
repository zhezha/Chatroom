package chatroom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * The chatroom client class 
 * Implements the actions of client GUI and Client Interface.
 * @author Zhao Zhengyang
 */
public class ChatroomClient extends UnicastRemoteObject implements ClientInterface {
    
    /**
     * Unique name for each client
     */
    private String clientName;
    /**
     * Instance of ClientInterface 
     * For server to invoke callback method
     */
    private static ClientInterface client;
    /**
     * Main GUI of client
     */
    private ClientFrame clientFrame;
    /**
     * Reference of remote object
     * For client to invoke remote method
     */
    private ServerInterface serverInterface;

    /**
     * A list consists of current existing chatrooms' names and creators
     */
    private ArrayList<String[]> chatroomList = new ArrayList<>();
    /**
     * The chatroom visiting
     * Showed on client GUI
     */
    private String currentChatroom;

    /**
     * Constructor, construct a ChatroomClient instance
     */
    public ChatroomClient() throws RemoteException, NotBoundException, MalformedURLException {

        super();
        clientFrame = new ClientFrame();
        clientFrame.setVisible(true);

        clientFrame.btnUnsubscribe.setEnabled(false);
        clientFrame.btnExit.setEnabled(false);
        clientFrame.btnRefreshParticipantsList.setEnabled(false);

        clientFrame.txtDisplayMsg.setText("");
        clientFrame.txtDisplayMsg.setLineWrap(true);
        clientFrame.txtDisplayMsg.setWrapStyleWord(true);
        clientFrame.txtWriteMsg.setText("");
        clientFrame.txtWriteMsg.setLineWrap(true);
        clientFrame.txtWriteMsg.setWrapStyleWord(true);

        //set client name here
        //each client should has a unique name
        clientName = "client3"; //**********************************************
        
        clientFrame.lblClientName.setText(clientName);

        //set rmi address ******************************************************
        serverInterface = (ServerInterface) Naming.lookup("rmi://localhost:1099/chatroom");

        //initialization process
        serverInterface.clientStart(clientName);
        chatroomList = serverInterface.getChatroomList();
        DefaultTableModel dtm = (DefaultTableModel) clientFrame.tbChatroomTable.getModel();
        for (int i = 0; i < chatroomList.size(); i++) {

            String chatroomName = chatroomList.get(i)[0];
            String creator = chatroomList.get(i)[1];
            dtm.addRow(new Object[]{
                chatroomName, creator
            });
        }

        currentChatroom = "";
        clientFrame.lblCurrentChatroom.setText("");
        clientFrame.lblSearchResult.setText("");

        handleEvent();
    }

    /**
     * Server calls this method of client to show notification
     * @param msg notification
     */
    public void showMsg(String msg) {

        //msg1 used in thread should be final variable
        final String msg1 = msg;
        new Thread(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(clientFrame, msg1);
            }
        }).start();
    }

    /**
     * When currentChatroom disappears with its creator,
     * server invokes this method to update the states of 
     * corresponding participants.
     */
    public void handleChatroomElimination() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                DefaultListModel dlm = new DefaultListModel();
                clientFrame.listPaticipants.setModel(dlm);
                dlm.removeAllElements();

                currentChatroom = "";
                clientFrame.lblCurrentChatroom.setText("");

                clientFrame.btnEnter.setEnabled(true);
                clientFrame.btnExit.setEnabled(false);
                clientFrame.btnRefreshParticipantsList.setEnabled(false);
                clientFrame.txtDisplayMsg.setText("");
            }
        }).start();
    }

    /**
     * Display msg on message display area
     * @param msg
     */
    public void displayMsg(String msg) {

        final String str = msg;
        new Thread(new Runnable() {

            @Override
            public void run() {
                clientFrame.txtDisplayMsg.append(str + "\n");
                clientFrame.txtDisplayMsg.paintImmediately(clientFrame.txtDisplayMsg.getBounds());
                clientFrame.txtDisplayMsg.setCaretPosition(clientFrame.txtDisplayMsg.getText().length());
            }
        }).start();
    }
    
    
    /**
     * Add action to each button on the client GUI to this method.
     */
    public void handleEvent() {

        //Quit Button
        clientFrame.btnQuit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    serverInterface.clientQuit(clientName, client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        });

        //Subscribe Button
        clientFrame.btnSubscribe.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    serverInterface.subscribe(client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                clientFrame.btnSubscribe.setEnabled(false);
                clientFrame.btnUnsubscribe.setEnabled(true);
            }
        });

        //Unsubscribe Button
        clientFrame.btnUnsubscribe.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    serverInterface.unsubscribe(client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                clientFrame.btnSubscribe.setEnabled(true);
                clientFrame.btnUnsubscribe.setEnabled(false);
            }
        });

        //Refresh chatroomTable Button
        clientFrame.btnRefreshChatroomTable.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    chatroomList = serverInterface.getChatroomList();
                } catch (RemoteException ex) {
                    Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                DefaultTableModel dtm = (DefaultTableModel) clientFrame.tbChatroomTable.getModel();
                dtm.setRowCount(0);
                for (int i = 0; i < chatroomList.size(); i++) {

                    String chatroomName = chatroomList.get(i)[0];
                    String creator = chatroomList.get(i)[1];
                    dtm.addRow(new Object[]{
                        chatroomName, creator
                    });
                }
            }
        });

        //Create Button
        clientFrame.btnCreate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String chatroomName = clientFrame.txtCreateChatroomName.getText();
                        try {
                            serverInterface.createChatroom(chatroomName, clientName);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String msg = "Chatroom created! Please refresh chatroom table.";
                        JOptionPane.showMessageDialog(clientFrame, msg);
                        clientFrame.txtCreateChatroomName.setText("");
                    }
                }).start();
            }
        });

        //Destroy Button
        clientFrame.btnDestroy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        int index = clientFrame.tbChatroomTable.getSelectedRow();
                        if (index == -1) {
                            JOptionPane.showMessageDialog(clientFrame, "No record selected!!");
                            return;
                        } else {
                            String chatroomName = (String) clientFrame.tbChatroomTable.getValueAt(index, 0);
                            String creator = (String) clientFrame.tbChatroomTable.getValueAt(index, 1);
                            if (creator.equalsIgnoreCase(clientName)) {
                                try {
                                    serverInterface.destroyChatroom(chatroomName);
                                } catch (RemoteException ex) {
                                    Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                String msg = "Chatroom destroyed! Please refresh chatroom table.";
                                JOptionPane.showMessageDialog(clientFrame, msg);
                            } else {
                                String msg = "Destroy unsuccessful, you are not chatroom creator!";
                                JOptionPane.showMessageDialog(clientFrame, msg);
                            }
                        }
                    }
                }).start();
            }
        });

        //Enter Button
        clientFrame.btnEnter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        ArrayList<String> participantList = new ArrayList<>();

                        int index = clientFrame.tbChatroomTable.getSelectedRow();
                        if (index == -1) {
                            JOptionPane.showMessageDialog(clientFrame, "No record selected!!");
                            return;
                        } else {
                            String chatroomName = (String) clientFrame.tbChatroomTable.getValueAt(index, 0);
                            try {
                                participantList = serverInterface.enterChatroom(chatroomName, clientName, client);
                            } catch (RemoteException ex) {
                                Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (participantList.get(0).equals("not exist")) {
                                JOptionPane.showMessageDialog(clientFrame, "Chatroom not exist! Please refresh chatroom table.");
                            } else {
                                DefaultListModel dlm = new DefaultListModel();
                                clientFrame.listPaticipants.setModel(dlm);
                                dlm.removeAllElements();
                                for (int i = 0; i < participantList.size(); i++) {
                                    dlm.addElement(participantList.get(i));
                                }
                                currentChatroom = chatroomName;
                                clientFrame.lblCurrentChatroom.setText(currentChatroom);
                                clientFrame.btnEnter.setEnabled(false);
                                clientFrame.btnExit.setEnabled(true);
                                clientFrame.btnRefreshParticipantsList.setEnabled(true);
                            }
                        }
                    }
                }).start();
            }
        });

        //Exit Button
        clientFrame.btnExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String chatroomName = currentChatroom;
                        try {
                            DefaultListModel dlm = new DefaultListModel();
                            clientFrame.listPaticipants.setModel(dlm);
                            dlm.removeAllElements();
                            serverInterface.exitChatroom(chatroomName, clientName);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        currentChatroom = "";
                        clientFrame.lblCurrentChatroom.setText(currentChatroom);
                        clientFrame.btnEnter.setEnabled(true);
                        clientFrame.btnExit.setEnabled(false);
                        clientFrame.btnRefreshParticipantsList.setEnabled(false);
                        clientFrame.txtDisplayMsg.setText("");
                    }
                }).start();
            }
        });

        //Refresh participantsList Button
        clientFrame.btnRefreshParticipantsList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String chatroomName = currentChatroom;
                        try {
                            DefaultListModel dlm = new DefaultListModel();
                            clientFrame.listPaticipants.setModel(dlm);
                            dlm.removeAllElements();
                            ArrayList<String> participantList = serverInterface.refreshParticipantsList(chatroomName);
                            for (int i = 0; i < participantList.size(); i++) {
                                dlm.addElement(participantList.get(i));
                            }
                        } catch (RemoteException ex) {
                            Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            }
        });

        //Send Button
        clientFrame.btnSend.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String receiver = clientFrame.txtMsgReceiver.getText();
                        String msg = clientFrame.txtWriteMsg.getText();
                        try {
                            serverInterface.sendMsg(currentChatroom, clientName, receiver, msg);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        clientFrame.txtMsgReceiver.setText("");
                        clientFrame.txtWriteMsg.setText("");
                    }
                }).start();
            }
        });
        
        //Search Button
        clientFrame.btnSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String result = null;
                        String name = clientFrame.txtSearchPerson.getText();
                        try {
                            result = serverInterface.SearchPerson(name);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ChatroomClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (result.equals("not exist")) {
                            JOptionPane.showMessageDialog(clientFrame, "User not exist!");
                        }
                        else if (result.equals("")) {
                            clientFrame.lblSearchResult.setText(name + " in no room");
                        }
                        else
                        {
                            clientFrame.lblSearchResult.setText(name +" in " + result);
                        }
                        clientFrame.txtSearchPerson.setText("");
                    }
                }).start();
            }
        });        

    }//handleEvent()


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        
        client = new ChatroomClient();
    }

}
