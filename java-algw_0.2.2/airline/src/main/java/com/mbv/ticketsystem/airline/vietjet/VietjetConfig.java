package com.mbv.ticketsystem.airline.vietjet;

public class VietjetConfig {
    private String username;
    private String password;
//    private String loginViewState;
    private String ipAddress;

    public VietjetConfig() {

    }

    public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public VietjetConfig(String username, String password,String ipAddress) {
        this.username = username;
        this.password = password;
//        this.loginViewState = loginViewState;
        this.ipAddress = ipAddress;
   
    }
    
    public VietjetConfig(String username, String password){
		this.username = username;
		this.password = password;
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
