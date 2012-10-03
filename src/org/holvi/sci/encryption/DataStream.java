package org.holvi.sci.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.holvi.sci.DataItem;
import org.holvi.sci.Utils;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;

/**
 * Wrapper for {@link InputStream}.
 * Contains available encryption keys for {@link DataItem.storeData()} and <code>DataItem.getData()</code> operations
 * @see DataItem
 */
public class DataStream {
	private static ArrayList<HolviKeyItem> keyList = new ArrayList<HolviKeyItem>();
	/**
	 * Initialization vector used for new {@link CipherInputStream} when encrypting data.
	 */
	private static byte[] iv = new byte[0];

	/**
	 * Encryption key used for new {@link CipherInputStream} when encrypting data.
	 */
	private static byte[] key = new byte[0];

	/**
	 * Hash of the {@link #iv} and {@link #key}
	 */
	private static String keyHash;
	private IvParameterSpec ips;
	private SecretKey skey;
	private Cipher cipher;
	private InputStream cis;

	/**
	 * 
	 * @param mode <code>link Cipher.DECRYPT_MODE</code> for decryption, <code>Cipher.ENCRYPT_MODE</code> for encryption
	 * @param is {@link InputStream} to be wrapped in {@link CipherInputStream}
	 * @param keyHash hash of the key to be used in decryption. Encryption uses {@link #iv} and {@link #key}
	 * @throws InvalidKeyException
	 * @throws HolviException
	 * @throws HolviEncryptionException
	 */
	public DataStream(int mode, InputStream is, String keyHash) 
			throws InvalidKeyException, HolviException, HolviEncryptionException {

		switch (mode) {
		case Cipher.DECRYPT_MODE:
			if (keyHash.equals("")) {
				this.ips = new IvParameterSpec(DataStream.iv);
				this.skey = new SecretKeySpec(DataStream.key, "AES");
			} else {
				HolviKeyItem keyItem = selectEncryptionKey(keyHash);
				if (keyItem != null) {
					this.ips = new IvParameterSpec(keyItem.getIV());
					this.skey = new SecretKeySpec(keyItem.getKey(), "AES");
				} else {
					if (is != null) {
						try {
							is.close();
						} catch (IOException exc) {

						}
					}
					throw new HolviEncryptionException(0, "No decryption key available");
				}
			}
			try {
				this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
				this.cipher.init(mode, this.skey, this.ips);
				this.cis = new CipherInputStream(is, this.cipher);
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} catch (NoSuchAlgorithmException e) {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} catch (NoSuchPaddingException e) {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} 
			break;

		case Cipher.ENCRYPT_MODE:
			if (DataStream.iv.length == 0|| DataStream.key.length == 0) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				HolviEncryptionException exc = new HolviEncryptionException(0, "Invalid cipher");
				throw exc;
			}
			this.ips = new IvParameterSpec(DataStream.iv);
			this.skey = new SecretKeySpec(DataStream.key, "AES");
			try {
				this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
				this.cipher.init(mode, this.skey, this.ips);
				this.cis = new CipherInputStream(is, this.cipher);
			} catch (InvalidAlgorithmParameterException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				handleException(e);
			} catch (NoSuchAlgorithmException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handleException(e);
			} catch (NoSuchPaddingException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handleException(e);
			}
			break;
		default:
			this.cis = is;
			break;
		}
	}

	/**
	 * Returns {@link InputStream}
	 * @return {@link #cis}
	 */
	public InputStream getStream() {
		return this.cis;
	}

	/**
	 * Sets {@link #iv}
	 * @param iv new initialization vector
	 */
	public static void setIV(byte[] iv) {
		DataStream.iv = iv;
		DataStream.keyHash = Utils.calculateKeyHash(DataStream.iv, DataStream.key);
	}

	/**
	 * sets {@link #key}
	 * @param key new encryption key
	 */
	public static void setEncryptionKey(byte[] key) {
		DataStream.key = key;
		DataStream.keyHash = Utils.calculateKeyHash(DataStream.iv, DataStream.key);
	}

	/**
	 * Returns length of the {@link #key}
	 * @return length of the {@link #key} in bytes
	 */
	public static int getKeyLength() {
		return key.length;
	}

	/**
	 * Returns calculated hash of the {@link #iv} and {@link #key}
	 * @return 
	 */
	public static String getKeyHash() {
		return keyHash;
	}

	/**
	 * Add new key to available keys
	 * @param iv initialization vector for the encryption key
	 * @param key encryption key
	 * @throws HolviEncryptionException
	 */
	public static void addEncryptionKey(byte[] iv, byte[] key) 
			throws HolviEncryptionException {
		String keyHash = Utils.calculateKeyHash(iv,  key);
		if (selectEncryptionKey(keyHash) != null) {
			throw new HolviEncryptionException(1, "Key already exists");
		}

		HolviKeyItem encryptionKey = new HolviKeyItem(iv, key, keyHash);
		keyList.add(encryptionKey);
	}

	/**
	 * Returns {@link HolviKeyItem} for the given <code>keyHash</code>
	 * @param keyHash hash of the wanted key
	 * @return {@link HolviKeyItem} with given <code>keyHash</code>, if none found returns <code>null</code>
	 */
	private static HolviKeyItem selectEncryptionKey(String keyHash) {
		for (HolviKeyItem keyItem : keyList) {
			if (keyItem.getKeyHash().equals(keyHash)) {
				return keyItem;
			}
		}
		return null;
	}

	/**
	 * Wraps thrown exceptions in {@link HolviEncryptionException}
	 * @param exc
	 * @throws HolviEncryptionException
	 */
	private void handleException(Exception exc) throws HolviEncryptionException {
		HolviEncryptionException encryptionException = new HolviEncryptionException();
		encryptionException.setMessage(exc.getMessage());
		throw encryptionException;
	}

	/**
	 * Removes encryption key from available keys
	 * @param keyHash hash of the {@link HolviKeyItem} to be removed
	 */
	public static void removeEncryptionKey(String keyHash) {
		HolviKeyItem keyItem = selectEncryptionKey(keyHash);
		if (keyItem != null) {
			keyList.remove(keyItem);
		}
	}
}
