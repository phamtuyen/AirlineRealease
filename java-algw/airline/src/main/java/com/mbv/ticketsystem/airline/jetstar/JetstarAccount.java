package com.mbv.ticketsystem.airline.jetstar;

public class JetstarAccount {

    private String username = "";
    private String password = "";

	private String loginViewState = "/wEPDwUBMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFM01lbWJlck5ld1RyYWRlTG9naW5Mb2dpbkFnZW50VmlldyRtZW1iZXJfUmVtZW1iZXJtZRIa6JIDpyg4u3VChloSe0QpildR";
    private String searchViewState = "/wEPDwUBMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFL01lbWJlckxvZ2luVHJhZGVTYWxlc0hvbWVWaWV3JG1lbWJlcl9SZW1lbWJlcm1l21vfn8zBj2cdGkkelpTFsbhWD1E=";

    public JetstarAccount(String username, String password) {
    	this.username = username;
        this.password = password;
    }
    
    public JetstarAccount() {    
    }

    public JetstarAccount(String username, String password, String loginViewState, String searchViewState) {
        this.username = username;
        this.password = password;
        this.loginViewState = loginViewState;
        this.searchViewState = searchViewState;
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
