package com.mbv.ticketsystem.common.base;

public class ContactInfo {
    private String address;
    private String city;
    private String mobile;
    private String email;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	@Override
	public String toString() {
		return "ContactInfo [address=" + address + ", city=" + city
				+ ", mobile=" + mobile + ", email=" + email + "]";
	}
    
    
}
