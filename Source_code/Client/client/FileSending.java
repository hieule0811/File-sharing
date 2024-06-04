package com.mycompany.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class FileSending implements Runnable {
    private Socket fileSendingSocket;
    private Peer senderPeer;

    public FileSending(Socket socket, Peer senderPeer) {
        this.fileSendingSocket = socket;
        this.senderPeer = senderPeer;
    }

    public void run() {
        try {
            PrintWriter op = new PrintWriter(this.fileSendingSocket.getOutputStream(), true);
            BufferedReader ip = new BufferedReader(new InputStreamReader(this.fileSendingSocket.getInputStream()));
            String fName = ip.readLine();
            String localFilePath = "";
            for (SharedFile sharedFile : this.senderPeer.getSharedFiles()) {
                if (sharedFile.compareFName(fName)) {
                    localFilePath = sharedFile.getLName();
                    break;
                }
            }
            
            System.out.println("Sharing file " + fName + "...");
            File fileToShare = new File(localFilePath);
            FileInputStream fileInputStream = new FileInputStream(fileToShare);
            OutputStream outputStream = fileSendingSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("Share successfully " + fName);

            fileInputStream.close();
            outputStream.close();
            ip.close();
            op.close();
            this.fileSendingSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
