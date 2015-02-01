package com.mbv.ticketsystem.airline.vietjet;

public class VietjetConfig {
    private String username;
    private String password;
    private String loginViewState;

    public VietjetConfig() {

    }

    public VietjetConfig(String username, String password, String loginViewState) {
        this.username = username;
        this.password = password;
        this.loginViewState = loginViewState;
   
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

    public String getLoginViewState() {
        return loginViewState;
    }

    public void setLoginViewState(String viewState) {
        this.loginViewState = viewState;
    }
}
