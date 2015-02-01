package com.mbv.ticketsystem.common.airline;

import java.util.List;

public class AirExtraServiceDescription {
    private String vendor;
    private String code;
    private Rule rule;
    private List<Option> options;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public static class Rule {
        private boolean required;
        private List<AirPassengerType> passengers;
        private String fares;

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public List<AirPassengerType> getPassengers() {
            return passengers;
        }

        public void setPassengers(List<AirPassengerType> passengers) {
            this.passengers = passengers;
        }

        public String getFares() {
            return fares;
        }

        public void setFares(String fares) {
            this.fares = fares;
        }
    }

    public static class Option {
        private String subCode;
        private long price;

        public String getSubCode() {
            return subCode;
        }

        public void setSubCode(String subCode) {
            this.subCode = subCode;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }
    }
}
