package org.holvi.sci;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.holvi.sci.exception.HolviClientException;
import org.holvi.sci.exception.HolviDataItemException;
import org.holvi.sci.exception.HolviException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides methods for communicating to Holvi.org server.
 *
 */
public class Connection {
	private static final String apiVersion = "1.1";
	private static CookieManager manager;
	private static CookieStore cookieJar;
	@SuppressWarnings("unused")
	private static List<HttpCookie> cookies;
	private HttpURLConnection conn;
	private BufferedReader rd;
	private JSONObject json;
	private static HolviClient client;
	private int authTries = 0;
	private static String url = "https://my.holvi.org";
	
	public Connection(HolviClient client) {
		Connection.client = client;
		Connection.manager = new CookieManager();
		Connection.manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(Connection.manager);
		Connection.cookieJar = manager.getCookieStore();
		Connection.cookies = cookieJar.getCookies();
	}
	
	public Connection () throws HolviClientException {
		if (client == null) {
			HolviClientException exc = new HolviClientException();
			exc.setMessage("Client not initialized");
			throw exc;
		}
	}
	
	/**
	 * Authenticates {@link HolviClient}
	 * @return <code>true</code> on successful authentication, else <code>false</code>.
	 * @throws HolviException
	 */
	public boolean auth() throws HolviException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("username", client.getUsername());
		map.put("auth_data", client.getPassword());
		map.put("auth_method", client.getAuthMethod());
		map.put("apikey", client.getApikey());
		authTries++;
		try {
			JSONObject response = (JSONObject) sendRequest("auth", map);
			JSONObject client = response.getJSONObject("client");
			JSONArray types = client.getJSONArray("types");
			String[] allowedTypes = new String[types.length()];
			for (int i=0; i < types.length(); i++) {
				allowedTypes[i] = types.getString(i);
			}
			Connection.client.setAllowedTypes(allowedTypes);
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sends JSON request to Holvi.org server.
	 * @param method request method.
	 * @param params parameters for the method used.
	 * @return result value(s) returned from server.
	 * @throws HolviException
	 */
	public Object sendRequest(String method, Map<String, Object> params) 
			throws HolviException {
		String line="", result = "";
		JSONObject response = null;
		json = new JSONObject();
		try {
			json.put("method", method);
			json.put("params", new JSONObject(params));
			URL url = new URL(Connection.url + "/api/" + Connection.apiVersion + "/json");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-type", "application/json; charset=utf-8");
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.getOutputStream().write(json.toString().getBytes("UTF8"));
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
			response = new JSONObject(result);
			if (response.has("error")) {
				return handleException(response, method, params);
			}
			
			return response.get("result");
			
		} catch (JSONException e) {
			HolviClientException exception = new HolviClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to Holvi.org server");
			throw exception;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			HolviClientException exception = new HolviClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to Holvi.org server");
			throw exception;
		}
		return null;
	}

	/**
	 * Does a fetch operation to Holvi.org server.
	 * @param headers request headers to be applied
	 * @return connection to Holvi.org server
	 * @throws HolviException
	 * @see {@link HttpURLConnection}
	 */
	public HttpURLConnection getData(Map<String, String> headers) 
			throws HolviException {
		Object[] keys = headers.keySet().toArray();
		URL url;
		try {
			url = new URL(Connection.url + "/api/" + Connection.apiVersion + "/fetch");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-type", "application/octet-stream; charset=utf-8");
			
			for (int i = 0; i < keys.length; i++) {
				conn.setRequestProperty((String) keys[i], headers.get(keys[i]));
			}
			
			String result = conn.getHeaderField("X-HOLVI-RESULT");
			if (!result.equals("OK")) {
				String[] exceptionData = result.split(" ", 4);
				int exceptionID = Integer.parseInt(exceptionData[1]);
				String message = exceptionData[3];
				if (exceptionID == 101) {
					while (authTries < 3) {
						if (auth()) {
							return getData(headers);
						}
					}
					authTries = 0;
				}
				HolviDataItemException exception = new HolviDataItemException();
				exception.setId(exceptionID);
				exception.setMessage(message);
				throw exception;
			}
			authTries = 0;
			return conn;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			HolviClientException exception = new HolviClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to Holvi.org server");
			throw exception;
		}
		return null;
	}

	/**
	 * Does a store operation to Holvi.org server.
	 * @param headers request headers to be applied
	 * @param dataChunk data to be sent
	 * @param len length of the data
	 * @throws HolviException
	 */
	public void sendData(Map<String, String> headers, byte[] dataChunk, int len) 
			throws HolviException {
		Object[] keys = headers.keySet().toArray();
		URL url;
		
		try {
			url = new URL(Connection.url + "/api/" + Connection.apiVersion + "/store");
			conn = (HttpURLConnection) url.openConnection();
			for (int i = 0; i < keys.length; i++) {
				conn.setRequestProperty((String) keys[i], headers.get(keys[i]));
			}
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream (
					conn.getOutputStream ());
			wr.write(dataChunk, 0, len);
			String result = conn.getHeaderField("X-HOLVI-RESULT");
			
			wr.close();
			if (conn.getResponseCode() != 200 || !result.equals("OK")) {
				HolviDataItemException exception;
				if (conn.getResponseCode() == 200) {
					String[] exceptionData = result.split(" ", 4);
					int exceptionID = Integer.parseInt(exceptionData[1]);
					String message = exceptionData[3];
					if (exceptionID == 101) {
						while (authTries < 3) {
							if (auth()) {
								sendData(headers, dataChunk, len);
								return;
							}
						}
						authTries = 0;
					}
					exception = new HolviDataItemException();
					exception.setId(exceptionID);
					exception.setMessage(message);
				} else {
					exception = new HolviDataItemException();
					exception.setId(conn.getResponseCode());
					exception.setMessage(conn.getResponseMessage());
				}
				throw exception;
			} 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			HolviClientException exception = new HolviClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to Holvi.org server");
			authTries = 0;
			throw exception;
		}

	}
	
	/**
	 * Parses the Holvi.org server exception response and wraps it in {@link HolviException}.<p>
	 * Tries re-authing in case of authentication failure.
	 * @param response server response to be parsed
	 * @param method original request method
	 * @param params original request parameters
	 * @return in case of authentication failure, tries re-authing and returns the response of original call.
	 * @throws HolviException
	 * @throws JSONException
	 */
	public Object handleException(JSONObject response, String method, Map<String, Object> params) 
			throws HolviException, JSONException {
		JSONObject exception = response.getJSONObject("error");
		String type = exception.getString("data");
		String message = exception.getString("message");
		int id = exception.getInt("code");
		
		if (id == 101) {
			while (authTries < 3) {
				auth();
				Object excResponse = sendRequest(method, params);
				authTries = 0;
				return excResponse;
			}
		}
		
		HolviException exc = new HolviException();
		exc.setId(id);
		exc.setMessage(message);
		exc.setType(type);
		authTries = 0;
		throw exc;
	}
	
	/**
	 * Updates {@link #url} associated with the {@link Connection}
	 * @param serverUrl new server url
	 */
	public void setServerUrl(String serverUrl) {
		Connection.url = serverUrl;
	}
}
