
package com.mbv.ticketsystem.airline.vnair.vniscsoapservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AgentLoginResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "agentLoginResult"
})
@XmlRootElement(name = "AgentLoginResponse")
public class AgentLoginResponse {

    @XmlElement(name = "AgentLoginResult")
    protected String agentLoginResult;

    /**
     * Gets the value of the agentLoginResult property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAgentLoginResult() {
        return agentLoginResult;
    }

    /**
     * Sets the value of the agentLoginResult property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAgentLoginResult(String value) {
        this.agentLoginResult = value;
    }

}
