package com.mycompany.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Server {

    private int port = 6666;
    private List<PeerRecord> peerRecords = new LinkedList<>();
    private ServerSocket serverSocket;
    private Map<String, PeerHandler> peerHandlers = new HashMap<>();
    public ServerTest serverForm;
    
    public Server(ServerTest serverForm){
        this.serverForm = serverForm;
    }
    
    public void startSV() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("Server is running on port " + this.port);
            ServerAccept serverAccept = new ServerAccept(this);
            new Thread(serverAccept).start();
            Scanner sc = new Scanner(System.in);
            /*
            while (true) {
                String cmd = sc.nextLine();
                String[] cmdArr = cmd.split(" ");
                switch (cmdArr[0]) {
                    case "ping":
                        if (cmdArr.length == 2) {
                            this.pingAction(cmdArr[1]);
                        } else {
                            System.out.println("Invalid command");
                        }
                        break;
                    case "discover":
                        if (cmdArr.length == 2) {
                            this.discoverAction(cmdArr[1]);
                        } else {
                            System.out.println("Invalid command");
                        }                        
                        break;
                    default:
                        System.out.println("Invalid command");
                        break;
                }
            }
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, PeerHandler> getPeerHandlers() {
        return this.peerHandlers;
    }

    public List<PeerRecord> getList() {
        return this.peerRecords;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public void pingAction(String id) {
        PeerHandler peerHandler = peerHandlers.get(id);
        if (peerHandler != null) {
            peerHandler.ping();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid ID");
        }
        if (peerHandler.peerIsLive()) {
            JOptionPane.showMessageDialog(null, "Ping successfully to " + id);            
        } else {            
            JOptionPane.showMessageDialog(null, "Ping failed to " + id);
        }
    }

    public void discoverAction(String id) {
        PeerHandler peerHandler = peerHandlers.get(id);
        if (peerHandler != null) {
            List<String> sharedFiles = peerHandler.getSharedFiles();
            System.out.println("Shared files of " + id + ":");
            for (String str : sharedFiles) {
                System.out.println(str);
                this.serverForm.addFileToList(str);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid ID");
        }
    }

    class ServerAccept implements Runnable {

        private Server server;
        

        public ServerAccept(Server server) {
            this.server = server;
        }

        public void run() {
            while (true) {
                Socket socket;
                try {
                    socket = server.getServerSocket().accept();
                    PrintWriter op = new PrintWriter(socket.getOutputStream(), true);

                    PeerRecord peerRecord = new PeerRecord(socket);
                    server.getList().add(peerRecord);
                    op.println(peerRecord.getId());
                    System.out.println("New peer connected, id: " + peerRecord.getId());
                    PeerHandler peerHandler = new PeerHandler(Server.this, peerRecord);
                    peerHandlers.put(peerRecord.getId(), peerHandler);
                    new Thread(peerHandler).start();
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           server.serverForm.addPeerToList(peerRecord.getId());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
