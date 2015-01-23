
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
 *         &lt;element name="BookingInfoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "bookingInfoResult"
})
@XmlRootElement(name = "BookingInfoResponse")
public class BookingInfoResponse {

    @XmlElement(name = "BookingInfoResult")
    protected String bookingInfoResult;

    /**
     * Gets the value of the bookingInfoResult property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBookingInfoResult() {
        return bookingInfoResult;
    }

    /**
     * Sets the value of the bookingInfoResult property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBookingInfoResult(String value) {
        this.bookingInfoResult = value;
    }

}
