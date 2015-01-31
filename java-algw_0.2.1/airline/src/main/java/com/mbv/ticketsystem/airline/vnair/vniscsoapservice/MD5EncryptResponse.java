
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
 *         &lt;element name="MD5EncryptResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "md5EncryptResult"
})
@XmlRootElement(name = "MD5EncryptResponse")
public class MD5EncryptResponse {

    @XmlElement(name = "MD5EncryptResult")
    protected String md5EncryptResult;

    /**
     * Gets the value of the md5EncryptResult property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMD5EncryptResult() {
        return md5EncryptResult;
    }

    /**
     * Sets the value of the md5EncryptResult property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMD5EncryptResult(String value) {
        this.md5EncryptResult = value;
    }

}
