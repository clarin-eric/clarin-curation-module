/**
 * 
 */
package eu.clarin.cmdi.curation.component_registry;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "profileDescriptions")
public class CCRProfiles {
    @XmlElement
    Collection<ProfileDescription> profileDescription;
    

    @XmlAccessorType(XmlAccessType.FIELD)
    static class ProfileDescription {
	
	String id;
    }

}