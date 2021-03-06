/**
 * This code was initially copied from the egit-github project
 * 
 *  http://git.eclipse.org/c/egit/egit-github.git/
 * 
 * and then adjusted for this API. The original contained this copyright:
 *
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *   Thomas Hallgren (Cloudsmith Inc.) - Stackhammer changes
 */
package org.cloudsmith.geppetto.forge.v2.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.cloudsmith.geppetto.forge.v2.client.Authenticator.AuthResponse;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * Class responsible for all request and response processing
 */
public class ForgeHttpClient implements Constants, ForgeClient {
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public static HttpClient createHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			httpClient.getConnectionManager().getSchemeRegistry().register(
				new Scheme("https", 443, new SSLSocketFactory(new TrustSelfSignedStrategy())));
		}
		catch(Exception e) {
			// let's try without that ...
		}
		return httpClient;
	}

	static InputStream getStream(HttpEntity entity) throws IOException {
		if(entity == null)
			return null;

		return entity.getContent();
	}

	private final String baseUri;

	private final Gson gson;

	private final HttpClient httpClient;

	private final Authenticator authenticator;

	private final ForgePreferences preferences;

	private String credentials;

	private String userAgent;

	@Inject
	public ForgeHttpClient(Gson gson, ForgePreferences preferences, Authenticator authenticator) {
		String baseUri = preferences.getBaseURL();
		if(baseUri == null)
			throw new IllegalArgumentException("Forge preference for base URL has not been set");
		if(!baseUri.endsWith("/"))
			baseUri += "/";

		this.baseUri = baseUri;
		this.gson = gson;
		this.authenticator = authenticator;
		this.preferences = preferences;
		userAgent = USER_AGENT;
		httpClient = createHttpClient();
	}

	protected void assignJSONContent(HttpEntityEnclosingRequestBase request, Object params) {
		if(params != null) {
			request.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON + "; charset=" + UTF_8.name()); //$NON-NLS-1$
			byte[] data = toJson(params).getBytes(UTF_8);
			request.setEntity(new ByteArrayEntity(data));
		}
	}

	public void authenticate() throws IOException {
		if(credentials == null) {
			AuthResponse auth = authenticator.authenticate(preferences.getLogin(), preferences.getPassword());
			String oauthToken = auth.getToken();
			preferences.setOAuthAccessToken(oauthToken);
			preferences.setOAuthScopes(auth.getScopes());
			this.credentials = "Bearer " + oauthToken;
		}
	}

	protected void configureRequest(final HttpRequestBase request) {
		if(credentials != null)
			request.addHeader(HttpHeaders.AUTHORIZATION, credentials);
		else

			request.addHeader(HttpHeaders.USER_AGENT, userAgent);
	}

	private HttpGet createGetRequest(String urlStr, Map<String, String> params) {
		URI uri;
		try {
			uri = new URI(createUri(urlStr));
			if(params != null && !params.isEmpty()) {
				List<NameValuePair> queryParams = new ArrayList<NameValuePair>(params.size());
				for(Map.Entry<String, String> param : params.entrySet())
					queryParams.add(new BasicNameValuePair(param.getKey(), param.getValue()));

				uri = new URI(
					uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
					URLEncodedUtils.format(queryParams, UTF_8.name()), uri.getFragment());
			}
		}
		catch(URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		return new HttpGet(uri);
	}

	/**
	 * Create full URI from path
	 * 
	 * @param path
	 * @return uri
	 */
	protected String createUri(final String path) {
		return baseUri + path;
	}

	@Override
	public void delete(final String uri) throws IOException {
		HttpDelete request = new HttpDelete(createUri(uri));
		configureRequest(request);
		executeRequest(request, null);
	}

	@Override
	public void download(String urlStr, Map<String, String> params, final OutputStream output) throws IOException {
		HttpGet request = createGetRequest(urlStr, params);
		configureRequest(request);
		httpClient.execute(request, new ResponseHandler<Void>() {
			@Override
			public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				StatusLine statusLine = response.getStatusLine();
				int code = statusLine.getStatusCode();
				if(code != HttpStatus.SC_OK)
					throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());

				HttpEntity entity = response.getEntity();
				entity.writeTo(output);
				return null;
			}
		});
	}

	protected <V> V executeRequest(final HttpRequestBase request, final Type type) throws IOException {
		return httpClient.execute(request, new JSonResponseHandler<V>(gson, type));
	}

	@Override
	public <V> V get(String urlStr, Map<String, String> params, Type type) throws IOException {
		HttpGet request = createGetRequest(urlStr, params);
		configureRequest(request);
		return executeRequest(request, type);
	}

	@Override
	public <V> V patch(final String uri, final Object params, final Class<V> type) throws IOException {
		HttpPatch request = new HttpPatch(createUri(uri));
		configureRequest(request);
		assignJSONContent(request, params);
		return executeRequest(request, type);
	}

	@Override
	public void post(String uri) throws IOException {
		postJSON(uri, null, null);
	}

	@Override
	public <V> V postJSON(final String uri, final Object params, final Class<V> type) throws IOException {
		HttpPost request = new HttpPost(createUri(uri));
		configureRequest(request);
		assignJSONContent(request, params);
		return executeRequest(request, type);
	}

	@Override
	public <V> V postUpload(String uri, Map<String, String> stringParts, InputStream in, String mimeType,
			String fileName, final long fileSize, Class<V> type) throws IOException {
		HttpPost request = new HttpPost(createUri(uri));
		configureRequest(request);

		MultipartEntity entity = new MultipartEntity();
		for(Map.Entry<String, String> entry : stringParts.entrySet())
			entity.addPart(entry.getKey(), StringBody.create(entry.getValue(), "text/plain", UTF_8));

		entity.addPart("file", new InputStreamBody(in, mimeType, fileName) {
			@Override
			public long getContentLength() {
				return fileSize;
			}
		});
		request.setEntity(entity);
		return executeRequest(request, type);
	}

	@Override
	public void put(String uri) throws IOException {
		put(uri, null, null);
	}

	@Override
	public <V> V put(final String uri, final Object params, final Class<V> type) throws IOException {
		HttpPut request = new HttpPut(createUri(uri));
		configureRequest(request);
		assignJSONContent(request, params);
		return executeRequest(request, type);
	}

	@Override
	public ForgeClient setUserAgent(final String agent) {
		if(agent != null && agent.length() > 0)
			userAgent = agent;
		else
			userAgent = USER_AGENT;
		return this;
	}

	/**
	 * Convert object to a JSON string
	 * 
	 * @param object
	 * @return JSON string
	 * @throws IOException
	 */
	protected String toJson(Object object) {
		return gson.toJson(object);
	}
}
