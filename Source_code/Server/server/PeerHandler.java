package com.mycompany.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PeerHandler implements Runnable{

    private Server server;
    private PeerRecord peerRecord;
    private PrintWriter op;
    private BufferedReader ip;
    private Timer timer;
    private volatile Boolean pingFinish;

    public PeerHandler(Server server, PeerRecord peerRecord) {
        this.server = server;
        this.peerRecord = peerRecord;
        try {
            this.op = new PrintWriter(this.peerRecord.getPSocket().getOutputStream(), true);
            this.ip = new BufferedReader(new InputStreamReader(this.peerRecord.getPSocket().getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pingFinish = false;
    }

    public void run() {
        try {
            while (true) {
                String inMess;
                inMess = ip.readLine();
                String[] inMessArr = inMess.split(" ");
                switch (inMessArr[0]) {
                    case "publish":
                        if (this.peerRecord.addSharedFile(inMessArr[1])) {
                            this.op.println("published " + inMessArr[1]);
                        } else {
                            this.op.println("published " + inMessArr[1] + " before");
                        }
                        break;
                    case "fetch":
                        PeerRecord peerRecordHaveFile = findPeerHaveFile(inMessArr[1]);
                        if (peerRecordHaveFile != null) {
                            String peerID = peerRecordHaveFile.getId();
                            PeerHandler peerHandlerHaveFile = this.server.getPeerHandlers().get(peerID);
                            peerHandlerHaveFile.sendMess("fetch-request " + inMessArr[1] + " from " + this.peerRecord.getId());
                        } else {
                            this.op.println("fetch-error not found peer have file " + inMessArr[1]);
                        }
                        break;
                    case "ping-reply":
                        synchronized (this) {
                            this.peerRecord.setLive(true);
                            this.timer.cancel();
                            this.pingFinish = true;
                        }
                        break;
                    case "fetch-reply":
                    // fetch-reply fName request-peer-id source-peer-ip source-peer-port
                        String fName = inMessArr[1];
                        String requestPeerID = inMessArr[2];
                        String sourcePeerIP = inMessArr[3];
                        int sourcePeerPort = Integer.parseInt(inMessArr[4]);
                        PeerHandler requestPeerHandler = this.server.getPeerHandlers().get(requestPeerID);
                        requestPeerHandler.sendMess("source-peer " + sourcePeerIP + " " + sourcePeerPort + " " + fName);
                        break;
                    default:
                        break;
                }  
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMess(String mess) {
        this.op.println(mess);
    }

    public List<String> getSharedFiles() {
        return this.peerRecord.getSharedFiles();
    }

    public boolean peerIsLive() {
        return this.peerRecord.isLive();
    }
    
    public void ping() {
        this.pingFinish = false;
        this.timer = new Timer();
        PingTask pingTask = new PingTask();
        this.op.println("ping-request");
        this.timer.schedule(pingTask, 3000);
        while (!this.pingFinish) {};
    }

    public PeerRecord findPeerHaveFile(String fName) {
        for (PeerRecord pR: this.server.getList()) {
            if (!(pR.getId().equals(this.peerRecord.getId())) && pR.containsFile(fName)) {
                PeerHandler peerHandler = this.server.getPeerHandlers().get(pR.getId());
                peerHandler.ping();
                if (pR.isLive()) {
                    return pR;
                }
            }
        }
        return null;
    }

    class PingTask extends TimerTask {

        public void run() {
            synchronized (PeerHandler.this) {
                PeerHandler.this.peerRecord.setLive(false);
                PeerHandler.this.pingFinish = true;
            }
        }
    }
}
