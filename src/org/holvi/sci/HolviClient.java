package org.holvi.sci;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.holvi.sci.container.Cluster;
import org.holvi.sci.container.Vault;
import org.holvi.sci.encryption.DataStream;
import org.holvi.sci.exception.HolviClientException;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides methods for manipulating generic Client attributes. <p>
 * Provides {@link Connection} to {@link DataItem}, {@link Cluster} and {@link Vault} operations.
 * 
 * @see Cluster
 * @see DataItem
 * @see DataStream
 * @see Vault
 */
public class HolviClient {
	public static enum ENC {
		NONE, AES_128, AES_192, AES_256
	}
	public static final int metaVersion = 1;
	private Connection connection;
	
	private String[] allowedTypes = new String[0];
	private String apikey = "atk8vzrhnc2by4f";
	private String username;
	private String authData;
	private String authMethod = "password";
	private ENC encryptionMode = ENC.NONE;
	
	public HolviClient(String username, String authmethod, String authdata, String apikey) {
		this.setUsername(username);
		this.setAuthMethod(authmethod);
		this.setPassword(authdata);
		this.setApikey(apikey);
		this.connection = new Connection(this);
	}
	
	public HolviClient() {
		this.connection = new Connection(this);
	}
	
	/**
	 * Returns available vault types
	 * @return available vault types
	 */
	public String[] getAllowedTypes() {
		return this.allowedTypes;
	}
	
	/**
	 * Sets available vault types
	 * @param types
	 */
	public void setAllowedTypes(String[] types) {
		this.allowedTypes = types;
	}
	
	/**
	 * Return new {@link Connection}
	 * @return
	 * @throws HolviClientException
	 */
	public Connection getConnection() throws HolviClientException {
		return new Connection();
	}
	
	/**
	 * Set {@link HolviException} encryption mode.
	 * @param mode new mode
	 */
	public void setEncryptionMode(ENC mode) {
		this.encryptionMode = mode;
	}

	/**
	 * Return current encryption mode
	 * @return current encryption mode
	 */
	public ENC getEncryptionMode() {
		return this.encryptionMode;
	}
	
	/**
	 * Return current API key
	 * @return
	 */
	public String getApikey() {
		return apikey;
	}
	
	/**
	 * Set API key used by {@link Connection}.
	 * @param apikey new API key.
	 */
	public void setApikey(String apikey) {
		if (apikey.equals(new String(""))) {
			this.apikey = new String("atk8vzrhnc2by4f");
		} else {
			this.apikey = apikey.trim();
		}
	}

	/**
	 * Returns current username.
	 * @return current username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets username used by {@link Connection}.
	 * @param username new username
	 */
	public void setUsername(String username) {
		this.username = username.trim();
	}

	/**
	 * Sets password used by {@link Connection}.
	 * @param authData new password
	 */
	public void setPassword(String authData) {
		this.authData = authData;
	}

	/**
	 * Returns authentication method used by {@link Connection}
	 * @return authentication method used
	 */
	public String getAuthMethod() {
		return authMethod;
	}

	/**
	 * Sets authentication method used by {@link Connection}
	 * @param authMethod
	 */
	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod.trim();
	}
	
	/**
	 * Sets the initialization vector used for encrypting data.
	 * @param iv new initialization vector
	 */
	public void setIV(byte[] iv) {
		DataStream.setIV(iv);
	}
	
	/**
	 * Sets the encryption key used for encrypting data.
	 * @param key new encryption key
	 */
	public void setEncryptionKey(byte[] key) {
		DataStream.setEncryptionKey(key);
	}
	
	/**
	 * Adds a new encryption key to available keys list.
	 * @param iv initialization vector
	 * @param key encryption key
	 * @throws HolviEncryptionException
	 * @see {@link DataStream}
	 */
	public void addEncryptionKey(byte[] iv, byte[] key) throws HolviEncryptionException {
		DataStream.addEncryptionKey(iv, key);
	}
	
	/**
	 * Authenticates {@link HolviClient} {@link #connection}
	 * @throws HolviException
	 */
	public synchronized void auth() throws HolviException {
		connection.auth();
	}

	/**
	 * Retrieves list of {@link Vault}s from Holvi.org server
	 * @param parameters
	 * @return
	 * @throws HolviException
	 * @throws IOException
	 */
	public Vault[] listVaults(Map<String, String> parameters) throws HolviException, IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("vault_type", parameters.get("vault_type"));
		map.put("role", parameters.get("role"));
		map.put("id_", parameters.get("id"));
		Object response;
		try {
			response = connection.sendRequest("list_vaults", map);
			JSONArray vaults = (JSONArray)response;
			Vault result[] = new Vault[vaults.length()];
			for (int i = 0; i < vaults.length(); i++) {
				JSONObject object = vaults.getJSONObject(i);
				result[i] = new Vault(this, object);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Add a new {@link Vault} to Holvi.org server
	 * @param name name of the vault
	 * @param vault_type type of the vault
	 * @return the vault that was created
	 * @throws HolviException
	 * @throws JSONException
	 * @throws IOException
	 */
	public Vault addVault(String name, String vault_type) throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("vault_type", vault_type);
		JSONObject response = (JSONObject) connection.sendRequest("add_vault", params);
		return new Vault(this, response);
	}
	
	/**
	 * Add a new {@link Cluster} to Holvi.org server
	 * @param name name of the cluster
	 * @param parentId parent id for the cluster
	 * @return the cluster that was created
	 * @throws HolviException
	 * @throws JSONException
	 * @throws IOException
	 */
	public Cluster addCluster(String name, int parentId) throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", Integer.toString(parentId));
		params.put("name", name);
		JSONObject response = (JSONObject) connection.sendRequest("add_cluster", params);
		return new Cluster(this, response);
	}

	protected String getPassword() {
		return this.authData;
	}
	
	public int getMetaVersion() {
		return HolviClient.metaVersion;
	}

	/**
	 * Creates a meta header for {@link DataItem} using old meta header.
	 * @param existingMeta old meta header
	 * @return new meta header
	 * @throws HolviEncryptionException
	 */
	public String createMeta(String existingMeta) throws HolviEncryptionException {
		HashMap<String, String> map = new HashMap<String, String>();
		if (existingMeta != null) {
			map = Utils.metaToMap(existingMeta);
			map.remove("v");
			map.remove("ENC");
			map.remove("KHA");
		}
		map.put("v", "v" + Integer.toString(metaVersion));
		String mode = "";
		
		switch (getEncryptionMode()) {
		case NONE:
			mode = "NONE";
			break;
		case AES_128:
			mode = "AES128";
			break;
		case AES_192:
			mode = "AES192";
			break;
		case AES_256:
			mode = "AES256";
			break;
		default:
			throw new HolviEncryptionException(1, "Invalid encryption mode");
		}
		
		map.put("ENC", mode);
		if (!getEncryptionMode().equals(ENC.NONE)) {
			map.put("KHA", DataStream.getKeyHash());
		}
		return Utils.metaToString(map);
	}

	/**
	 * Set server url used by {@link Connection}
	 * @param serverUrl new server url
	 */
	public void setServerUrl(String serverUrl) {
		connection.setServerUrl(serverUrl);
	}

	/**
	 * Remove encryption key from available keys
	 * @param keyHash hash of the key to be removed
	 */
	public void removeEncryptionKey(String keyHash) {
		DataStream.removeEncryptionKey(keyHash);
	}
}
