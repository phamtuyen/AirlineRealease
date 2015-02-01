package com.mbv.ticketsystem.airline.jetstar;

public class JetstarAccount {
    private String username = "";
    private String password = "";
    
    public JetstarAccount(String username, String password) {
    	this.username = username;
        this.password = password;
    }
    
    public JetstarAccount() {    
    	
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
