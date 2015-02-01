
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
 *         &lt;element name="OrderID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="TokenKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Hash" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "orderID",
        "tokenKey",
        "hash"
})
@XmlRootElement(name = "BookingInfo")
public class BookingInfo {

    @XmlElement(name = "OrderID")
    protected long orderID;
    @XmlElement(name = "TokenKey")
    protected String tokenKey;
    @XmlElement(name = "Hash")
    protected String hash;

    /**
     * Gets the value of the orderID property.
     */
    public long getOrderID() {
        return orderID;
    }

    /**
     * Sets the value of the orderID property.
     */
    public void setOrderID(long value) {
        this.orderID = value;
    }

    /**
     * Gets the value of the tokenKey property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTokenKey() {
        return tokenKey;
    }

    /**
     * Sets the value of the tokenKey property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTokenKey(String value) {
        this.tokenKey = value;
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
