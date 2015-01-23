
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
 *         &lt;element name="Agent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Hash" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "agent",
        "hash"
})
@XmlRootElement(name = "AgentLogin")
public class AgentLogin {

    @XmlElement(name = "Agent")
    protected String agent;
    @XmlElement(name = "Hash")
    protected String hash;

    /**
     * Gets the value of the agent property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Sets the value of the agent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAgent(String value) {
        this.agent = value;
    }

    /**
     * Gets the value of the hash property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the value of the hash property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHash(String value) {
        this.hash = value;
    }

}
