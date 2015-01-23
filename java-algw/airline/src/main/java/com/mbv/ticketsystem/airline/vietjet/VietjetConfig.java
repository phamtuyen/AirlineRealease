package com.mbv.ticketsystem.airline.vietjet;

public class VietjetConfig {
    private String username;
    private String password;

	private String loginViewState = "/wEPDwUKMTMwMDM4OTMzNGRkDNea6hrkC/EuG33xwbn0ZVbqiEc=";
    private String searchViewState = "/wEPDwULLTEyMjM5MjYzNTIPZBYCAgEPZBYQAgYPEGRkFgBkAgcPEGRkFgBkAgoPEGRkFgBkAgsPEGRkFgBkAgwPEGRkFgBkAg0PEGRkFgBkAg4PEGRkFgBkAhMPEGRkFgBkZEaf8zaQs6Kz1vM9AQ6RzJ+L6f79";

    public VietjetConfig() {

    }

    public VietjetConfig(String username, String password, String loginViewState, String searchViewState) {
        this.username = username;
        this.password = password;
        this.loginViewState = loginViewState;
        this.searchViewState = searchViewState;
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

    public String getSearchViewState() {
        return searchViewState;
    }

    public void setSearchViewState(String viewState) {
        this.searchViewState = viewState;
    }
}
