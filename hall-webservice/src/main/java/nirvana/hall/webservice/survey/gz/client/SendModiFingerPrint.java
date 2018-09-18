
package nirvana.hall.webservice.survey.gz.client;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendModiFingerPrint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendModiFingerPrint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="xckybh" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="modiFingerPrint" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendModiFingerPrint", propOrder = {
    "userID",
    "password",
    "xckybh",
    "modiFingerPrint"
})
public class SendModiFingerPrint {

    protected String userID;
    protected String password;
    protected String xckybh;
    @XmlMimeType("*/*")
    protected DataHandler modiFingerPrint;

    /**
     * Gets the value of the userID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the value of the userID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserID(String value) {
        this.userID = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the xckybh property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXckybh() {
        return xckybh;
    }

    /**
     * Sets the value of the xckybh property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXckybh(String value) {
        this.xckybh = value;
    }

    /**
     * Gets the value of the modiFingerPrint property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getModiFingerPrint() {
        return modiFingerPrint;
    }

    /**
     * Sets the value of the modiFingerPrint property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setModiFingerPrint(DataHandler value) {
        this.modiFingerPrint = value;
    }

}
