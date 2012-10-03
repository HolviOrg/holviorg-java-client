package org.holvi.sci;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.holvi.sci.HolviClient.ENC;
import org.holvi.sci.encryption.DataStream;
import org.holvi.sci.exception.HolviDataItemException;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides methods for handling data on Holvi.org server
 *
 */
public class DataItem {
	private HolviClient client;
	private int parentId;
	private String name;
	private long keyLength;
	private String lastModifiedDate;
	private String lastAccessedDate;
	private String hash;
	private String meta = "";

	public DataItem(HolviClient client) {

	}

	public DataItem(HolviClient client, JSONObject object, int parentId) throws JSONException {
		this.client = client;
		this.name = object.getString("name");
		this.meta = object.getString("meta");
		this.hash = object.getString("md5sum");
		this.keyLength = object.getLong("size");
		this.parentId = parentId;
		try {
			this.setLastModifiedDate(object.getString("modified_date"));
		} catch (JSONException e) {
			this.setLastModifiedDate("");
		}
		try {
			this.setLastAccessedDate(object.getString("last_accessed_date"));
		} catch (JSONException e) {
			this.setLastAccessedDate("");
		}

		this.setKeyLength(object.getLong("size"));
	}

	public DataItem(HolviClient client, String name, int parentId) {
		this.client = client;
		this.name = name;
		this.parentId = parentId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMeta() {
		return this.meta;
	}

	/**
	 * Stores data to {@link DataItem}
	 * @param method the store method used for store operation (<code>new</code> or <code>replace</code>)
	 * @param is the data to be sent
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws HolviException
	 * @throws HolviEncryptionException
	 */
	public void storeData(String method, InputStream is) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, HolviException, HolviEncryptionException {
		Map<String, String> headers = new HashMap<String, String>();
		String meta = "";
		synchronized (this) {
			meta = this.client.createMeta(this.meta);
		}
		headers.put("X-HOLVI-STORE-MODE", method);
		headers.put("X-HOLVI-KEY", Base64.encodeBase64String(this.name.getBytes("UTF-8")));
		headers.put("X-HOLVI-PARENT", Integer.toString(this.parentId));
		headers.put("X-HOLVI-META", meta);
		headers.put("Content-Type", "application/octet-stream");
		MessageDigest ciphermd = null;
		MessageDigest plainmd = null;
		try {
			ciphermd = MessageDigest.getInstance("MD5");
			plainmd = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}

		DigestInputStream digestStream = new DigestInputStream(is, plainmd);
		InputStream dataStream;
		if (client.getEncryptionMode() != HolviClient.ENC.NONE) {
			dataStream = new DataStream(Cipher.ENCRYPT_MODE, digestStream, null).getStream();
		} else {
			dataStream = new DataStream(0, digestStream, null).getStream();
		}

		int buffsize = 20971520;
		byte[] buff = new byte[buffsize];
		int len = 0;
		int inbuff = 0;
		while((len = dataStream.read(buff, 0, buffsize)) > 0) {
			inbuff = len;
			while ((inbuff < buffsize)) {
				len = dataStream.read(buff, inbuff, buffsize-inbuff);
				if (len == -1) {
					break;
				}
				inbuff += len;
			}
			ciphermd.update(buff, 0, inbuff);
			headers.put("Content-Length", Integer.toString(inbuff));
			headers.put("X-HOLVI-HASH", Utils.getHex(ciphermd.digest()));
			this.client.getConnection().sendData(headers, buff, inbuff);
			headers.put("X-HOLVI-STORE-MODE", "append");
		}
		this.meta = meta;
		HashMap<String, String> metamap = Utils.metaToMap(this.meta);
		metamap.put("CHA", Utils.getHex(plainmd.digest()));
		updateMeta(metamap);
	}

	/**
	 * Retrieves data from {@link DataItem}.
	 * @return {@link InputStream} of the server connection.
	 * @throws InvalidKeyException
	 * @throws HolviException
	 * @throws HolviEncryptionException
	 * @see {@link InputStream}
	 */
	public InputStream getData() 
			throws InvalidKeyException, HolviException, HolviEncryptionException {
		Map<String, String> headers = new HashMap<String, String>();
		try {
			headers.put("X-HOLVI-KEY", Base64.encodeBase64String(this.name.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		headers.put("X-HOLVI-PARENT", Integer.toString(this.parentId));

		HttpURLConnection conn = null;
		HashMap<String, String> metaMap;
		try {
			conn = this.client.getConnection().getData(headers);
		} catch (HolviException e) {
			e.printStackTrace();
		} 
		this.setHash(conn.getHeaderField("X-HOLVI-HASH"));
		this.meta = conn.getHeaderField("X-HOLVI-META");
		metaMap = Utils.metaToMap(this.meta);
		this.setKeyLength(Long.parseLong(conn.getHeaderField("Content-Length")));
		DataStream dataStream;
		InputStream is = null;
		try {
			is = conn.getInputStream();
		} catch (IOException e) {
			HolviDataItemException exc = new HolviDataItemException();
			exc.setMessage("Error with server connection");
			throw exc;
		}
		if (!metaMap.get("ENC").equalsIgnoreCase("NONE") && client.getEncryptionMode() != ENC.NONE) {
			if (metaMap.containsKey("KHA")) {
				dataStream = new DataStream(Cipher.DECRYPT_MODE, is, metaMap.get("KHA"));
			} else {
				dataStream = new DataStream(Cipher.DECRYPT_MODE, is, "");
			}
		} else {
			dataStream = new DataStream(0, is, null);
		}
		return dataStream.getStream();

	}

	/**
	 * Performs remove operation on {@link DataItem}.
	 * @throws HolviException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void remove() throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", Integer.toString(this.parentId));
		params.put("name", this.name);
		this.client.getConnection().sendRequest("remove_dataitem", params);

	}

	/**
	 * Updates meta header of {@link DataItem}.
	 * @param newMetaValues
	 * @throws HolviException
	 * @throws IOException
	 */
	public synchronized void updateMeta(HashMap<String, String> newMetaValues) throws HolviException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		HashMap<String, String> existingMeta = Utils.metaToMap(this.meta);

		for (String key: newMetaValues.keySet()) {
			if (newMetaValues.get(key) != null) {
				existingMeta.put(key, newMetaValues.get(key));
			}
		}
		this.meta = Utils.metaToString(existingMeta);
		params.put("parent_id", this.parentId);
		params.put("name", this.name);
		params.put("meta", this.meta);
		this.client.getConnection().sendRequest("update_dataitem", params);
	}

	/**
	 * Renames {@link DataItem}
	 * @param newName new name for the data item
	 * @throws HolviException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void rename(String newName) throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", this.parentId);
		params.put("name", this.name);
		params.put("new_name", newName);

		this.client.getConnection().sendRequest("rename_dataitem", params);
		this.name = newName;
	}

	public long getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(long keyLength) {
		this.keyLength = keyLength;
	}


	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getLastAccessedDate() {
		return lastAccessedDate;
	}

	public void setLastAccessedDate(String lastAccessedDate) {
		this.lastAccessedDate = lastAccessedDate;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
