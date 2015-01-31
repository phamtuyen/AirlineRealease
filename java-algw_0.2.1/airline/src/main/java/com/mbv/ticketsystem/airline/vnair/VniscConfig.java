package com.mbv.ticketsystem.airline.vnair;

import java.util.regex.Pattern;

public class VniscConfig extends VnairConfig {
    private String agentCode;
    private String securityCode;
    private Pattern redirectPattern;

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public Pattern getRedirectPattern() {
        return redirectPattern;
    }

    public void setRedirectPattern(Pattern redirectPattern) {
        this.redirectPattern = redirectPattern;
    }


}
