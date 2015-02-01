
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
 *         &lt;element name="ErrorNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "errorNumber"
})
@XmlRootElement(name = "GetErrorDescription")
public class GetErrorDescription {

    @XmlElement(name = "ErrorNumber")
    protected int errorNumber;

    /**
     * Gets the value of the errorNumber property.
     */
    public int getErrorNumber() {
        return errorNumber;
    }

    /**
     * Sets the value of the errorNumber property.
     */
    public void setErrorNumber(int value) {
        this.errorNumber = value;
    }

}
