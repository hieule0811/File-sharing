package com.mycompany.server;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class PeerRecord {
    static int count = 0;
    static ReentrantLock lock = new ReentrantLock();

    private Socket pSocket;
    private String pId;
    private List<String> sharedFiles;
    private boolean live;
    
    public PeerRecord(Socket pSocket) {
        this.pSocket = pSocket;
        lock.lock();
        this.pId = "peer" + count;
        count++;
        lock.unlock();
        this.sharedFiles = new LinkedList<>();
        this.live = true;
    }

    public Socket getPSocket() {
		return pSocket;
	}

	public String getId() {
		return pId;
	}

	public List<String> getSharedFiles() {
		return sharedFiles;
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

    public boolean addSharedFile(String fName) {
        if (this.sharedFiles.contains(fName)) {
            return false;
        } else {
            this.sharedFiles.add(fName);
            return true;
        }
    }

    public boolean containsFile(String fName) {
        return this.sharedFiles.contains(fName);
    }
}
