package org.holvi.client;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML element of a single encryption key.<p>
 * Provides location of the encryption key as well as description, length and hash of the key.
 *
 */
@XmlRootElement(name = "key")
public class HolviXMLKeyItem {
	private String path;
	private String description;
	private String keyHash;
	private int keyLength;
	private boolean exists;
	
	public HolviXMLKeyItem() {
	}
	
	public HolviXMLKeyItem(String path, String description) {
		this.path = path;
		this.description = description;
	}
	
	public HolviXMLKeyItem(String path, String description, String keyHash, int keyLength) {
		this.path = path;
		this.description = description;
		this.keyHash = keyHash;
		this.keyLength = keyLength;
	}
	
	@XmlElement(name = "keyPath")
	public String getPath() {
		return this.path;
	}
	public String getTableDescription() {
		return this.description + " (AES:" +this.keyLength + ")";
	}
	@XmlElement(name = "keyHash")
	public String getKeyHash() {
		return this.keyHash;
	}
	
	public void setKeyHash(String keyhash) {
		this.keyHash = keyhash;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	@XmlElement(name = "keyDescription")
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "keyLength")
	public int getKeyLength() {
		return this.keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	
	public void exists(boolean exists) {
		this.exists = exists;
	}
	
	public boolean isAvailable() {
		return this.exists;
	}
}