package org.holvi.client;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML element containing multiple {@link HolviXMLKeyItem}s
 * 
 */
@XmlRootElement
public class HolviXMLKeyList {
	@XmlElement(name = "key")
	private ArrayList<HolviXMLKeyItem> keyList;
	
	public void setKeyList(ArrayList<HolviXMLKeyItem> keyList) {
	    this.keyList = keyList;
	  }

	  public ArrayList<HolviXMLKeyItem> getKeysList() {
	    return keyList;
	  }

}
