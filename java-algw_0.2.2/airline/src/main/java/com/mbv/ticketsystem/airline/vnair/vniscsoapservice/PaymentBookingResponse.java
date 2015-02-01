
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
 *         &lt;element name="PaymentBookingResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "paymentBookingResult"
})
@XmlRootElement(name = "PaymentBookingResponse")
public class PaymentBookingResponse {

    @XmlElement(name = "PaymentBookingResult")
    protected String paymentBookingResult;

    /**
     * Gets the value of the paymentBookingResult property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPaymentBookingResult() {
        return paymentBookingResult;
    }

    /**
     * Sets the value of the paymentBookingResult property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPaymentBookingResult(String value) {
        this.paymentBookingResult = value;
    }

}
