package com.mycompany.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;

public class FileReceiving implements Runnable {
    private String fName;
    private String sourcePeerIp;
    private int sourcePeerPort;
    private Peer receiverPeer;

    public FileReceiving(String fName, String sourcePeerIp, int sourcePeerPort, Peer receiverPeer) {
        this.fName = fName;
        this.sourcePeerIp = sourcePeerIp;
        this.sourcePeerPort = sourcePeerPort;
        this.receiverPeer = receiverPeer;
    }

    public void run() {
        
        try {
            Socket fileSharingSocket = new Socket(this.sourcePeerIp, this.sourcePeerPort);
            PrintWriter fileSharingOp = new PrintWriter(fileSharingSocket.getOutputStream(), true);
            InputStream inputStream = fileSharingSocket.getInputStream();

            fileSharingOp.println(fName);

            String localRepository = this.receiverPeer.getLocalPath() + "\\" + fName;

            System.out.println("Downloading file " + fName + " from " + this.sourcePeerIp + "...");
            File receivedFile = new File(localRepository);
            FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            System .out.println("Download successfully " + fName);
            JOptionPane.showMessageDialog(null, "Download successfully " + fName);
            
            fileOutputStream.close();
            inputStream.close();
            fileSharingSocket.close();
            fileSharingOp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
