package com.mycompany.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

public class Peer {
    private int svPort = 6666;
    private ServerSocket serverSocket;
    private int uploadPort;
    private boolean onl;
    private String id;
    private String localPath;
    private List<SharedFile> sharedFiles = new LinkedList<>();
    private BufferedReader ip;
    private PrintWriter op;

    public Peer (int uploadPort, String localPath) {
        this.serverSocket = null;
        this.uploadPort = uploadPort;
        this.onl = false;
        this.id = "";
        this.localPath = localPath;
    }
    
    public void startPeer() {
        try {
            System.out.println("Connecting to server with port " + this.svPort);
            Socket socket = new Socket("localhost", this.svPort);

            ip = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            op = new PrintWriter(socket.getOutputStream(), true);

            String inMess = ip.readLine();
            this.id = inMess;
            this.onl = true;
            System.out.println("Your id: " + this.id);
            
            ServerListener serverListener = new ServerListener(socket, op);
            new Thread(serverListener).start();
            /*
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    String cmd = sc.nextLine();
                    String[] cmdArr = cmd.split(" ");
                    switch (cmdArr[0]) {
                        case "publish":
                            if (cmdArr.length == 3) {
                                this.publishAction(cmdArr[1], cmdArr[2]);
                            } else {
                                System.out.println("Invalid command");
                            }
                            break;
                        case "fetch":
                            if (cmdArr.length == 2) {
                                op.println("fetch " + cmdArr[1]);
                            } else {
                                System.out.println("Invalid command");
                            }
                            break;
                        case "disconect":
                            this.onl = false;
                            System.out.println("Disconnected");
                            break;
                        case "connect":
                            this.onl = true;
                            System.out.println("Connected");
                            break;
                        default:
                            System.out.println("Invalid command");
                            break;
                    }
                }
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void setUploadPort(int uploadPort) {
        this.uploadPort = uploadPort;
    }
    
    public int getUploadPort() {
        return this.uploadPort;
    }
    
    public void setOnl(boolean onl) {
        this.onl = onl;
    }

    public boolean isOnl() {
        return this.onl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public List<SharedFile> getSharedFiles() {
        return this.sharedFiles;
    }

    public String getIp() {
        if (this.serverSocket != null) {
            return this.serverSocket.getInetAddress().getHostAddress();
        } else {
            return null;
        }
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    

    public boolean addSharedFile(SharedFile sharedFile) {
        for (SharedFile runner : this.sharedFiles) {
            if (runner.compareFName(sharedFile.getFName())) {
                return false;
            }
        }
        this.sharedFiles.add(sharedFile);
        return true;
    }

    public synchronized void openServerSocket() {
        if (this.serverSocket == null) {
            try {
                this.serverSocket = new ServerSocket(this.uploadPort);
                PeerConnectAccept peerConnectAccept = new PeerConnectAccept(this);
                new Thread(peerConnectAccept).start();
                System.out.println("Listening...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void publishAction(String lName, String fName) {
        System.out.println("Publishing file...");
        lName = lName.replaceAll("\"", "");
        SharedFile sharedFile = new SharedFile(lName, fName);
        if (this.addSharedFile(sharedFile)) {
            op.println("publish " + fName);            
            JOptionPane.showMessageDialog(null, "Published file " + fName);
        } else {
            System.out.println("File already published");
            JOptionPane.showMessageDialog(null, "File already published");
        }
    }
    public void fetchAction(String fName){
        op.println("fetch " + fName);
    }

    class ServerListener implements Runnable {
        private Socket socket;
        private PrintWriter op;

        public ServerListener(Socket socket, PrintWriter op) {
            this.socket = socket;
            this.op = op;
        }

        public void run() {
            try {
                BufferedReader ip = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String inMess = ip.readLine();
                    String[] inMessArr = inMess.split(" ");
                    switch (inMessArr[0]) {
                        case "published":
                            System.out.println(inMess);
                            break;
                        case "ping-request":
                            if (Peer.this.onl) {
                                this.op.println("ping-reply");
                            }
                            break;
                        case "fetch-error":
                            System.out.println(inMess);
                            JOptionPane.showMessageDialog(null, 
                                    "Could not find the Peer that owns file " + inMessArr[ inMessArr.length - 1 ]);
                            break;
                        case "fetch-request":
                            Peer.this.openServerSocket();
                            // fetch-reply fName request-peer-id source-peer-ip source-peer-port
                            String reply = "fetch-reply " + inMessArr[1] + " " + inMessArr[3] + " " + Peer.this.getIp() + " " + Peer.this.getUploadPort();
                            this.op.println(reply);
                            break;
                        case "source-peer":
                            //source-peer source-peer-ip source-peer-port fName
                            String sourcePeerIp = inMessArr[1];
                            int sourcePeerPort = Integer.parseInt(inMessArr[2]);
                            String fName = inMessArr[3];

                            FileReceiving fileReceiving = new FileReceiving(fName, sourcePeerIp, sourcePeerPort, Peer.this);
                            new Thread(fileReceiving).start();
                            break;
                        default:
                            break;
                    }
                } 
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class PeerConnectAccept implements Runnable {
        private Peer senderPeer;

        public PeerConnectAccept(Peer senderPeer) {
            this.senderPeer = senderPeer;
        }

        public void run() {
            while (true) {
                try {
                    Socket socket = this.senderPeer.serverSocket.accept();
                    FileSending fileSending = new FileSending(socket, this.senderPeer);
                    new Thread(fileSending).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
