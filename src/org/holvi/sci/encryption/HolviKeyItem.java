package org.holvi.sci.encryption;

import org.holvi.sci.HolviClient.ENC;
import org.holvi.sci.exception.HolviEncryptionException;

/**
 * Model for an encryption key.
 * <p>Used by {@link DataStream}
 */
class HolviKeyItem {
	private String keyHash;
	private ENC encMode;
	private byte[] iv;
	private byte[] key;
	
	protected HolviKeyItem() {
	}
	
	protected HolviKeyItem(byte[] iv, byte[] key, String keyHash) throws HolviEncryptionException {
		this.iv = iv;
		this.key = key;
		this.keyHash = keyHash;
		switch (key.length) {
		case 16:
			this.encMode = ENC.AES_128;
			break;
		case 24:
			this.encMode = ENC.AES_192;
			break;
		case 32:
			this.encMode = ENC.AES_256;
			break;
		default:
			throw new HolviEncryptionException(0, "Invalid encryption key");
		}
	}
	
	protected String getKeyHash() {
		return this.keyHash;
	}
	
	protected ENC getEncMode() {
		return this.encMode;
	}

	protected byte[] getKey() {
		return this.key;
	}
	
	protected byte[] getIV() {
		return this.iv;
	}
}
