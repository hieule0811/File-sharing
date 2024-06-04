package com.mycompany.Client;

public class SharedFile {
    private String lName;
    private String fName;

    public SharedFile(String lName, String fName) {
        this.lName = lName;
        this.fName = fName;
    }
    
    public String getLName() {
        return this.lName;
    }

    public String getFName() {
        return this.fName;
    }

    public boolean compareFName(String fName) {
        if (this.fName.equals(fName)) {
            return true;
        }
        return false;
    }
}
