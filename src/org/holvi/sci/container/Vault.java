package org.holvi.sci.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.holvi.sci.HolviClient;
import org.holvi.sci.exception.HolviException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model for Holvi.org server Vault objects. <p>
 * Top-level container of data. <p>
 * Provides methods for structuring data. <p>
 *
 */
public class Vault extends Cluster {
	String vaultType;
	
	public Vault(HolviClient client) {
		this.client = client;
	}
	
	public Vault(HolviClient client, JSONObject object) throws JSONException {
		this.client = client;
		this.id = object.getInt("id");
		this.name = object.getString("name");
		this.childCount = object.getInt("descendants");
		this.dataItemCount = object.getInt("dataitems");
		this.permissions = object.getJSONArray("permissions");
		this.last_accessed_date = (object.get("last_accessed_date") != JSONObject.NULL ? object.getString("last_accessed_date"): "");
		this.last_modified_date = (object.get("modified_date") != JSONObject.NULL ? object.getString("modified_date"): "");
	}
	
	public void remove() throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vault_id", this.id);
		this.client.getConnection().sendRequest("remove_vault", params);
	}
	
	public void rename(String newName) throws HolviException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vault_id", this.id);
		params.put("vault_name", newName);
		this.client.getConnection().sendRequest("rename_vault", params);
		this.name = newName;
	}
	
	/**
	 * Vaults are top-level containers and cannot have parent container.
	 * @return -1
	 */
	@Override
	public int getParentId() {
		return -1;
	}
}
