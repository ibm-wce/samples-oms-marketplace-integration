/*******************************************************************************
 * Copyright IBM Corp. 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ibm.sterling.integration.marketplace.rest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.yantra.yfc.log.YFCLogCategory;

public class RESTURLConnection implements RESTConstants {
	private static YFCLogCategory cat = YFCLogCategory.instance(RESTURLConnection.class.getName());

	public static final String DEFAULT_PARAM_ENCODING = "utf-8";

	/**
	 * Returns an instance of rest client
	 *			
	 */
	public static RESTURLConnection getInstance() {
		RESTURLConnection rt = new RESTURLConnection();
		return rt;	
	}

	private RESTURLConnection() { }

	/**
	 * Executes a GET REST request.
	 * @param url
	 * @param params
	 * 			Query Parameters
	 * @param headers
	 * @param connectTimeout
	 * @param inputStreamReadTimeout
	 * @return HTTP response 
	 * @throws IOException
	 */
	public RESTResponse executeGET(String url, Map<String, List<String>> params,
			Map<String, List<String>> headers, int connectTimeout, int inputStreamReadTimeout) throws IOException {

		RESTResponse obj = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		
		try {
			String temp = addQueryParamsToURL(url, params);
			URL connectionUrl = new URL(temp);
			urlConnection = (HttpURLConnection) connectionUrl.openConnection();
			urlConnection.setConnectTimeout(connectTimeout);
			urlConnection.setReadTimeout(inputStreamReadTimeout);
			if (!headers.containsKey(CONTENT_TYPE)) {
				List<String> list = new ArrayList<String>();
				list.add(DEFAULT_CONTENT_TYPE);
				headers.put(CONTENT_TYPE, list);
			}
			StringBuilder sb = new StringBuilder();
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				for (String v : entry.getValue()) {
					if (sb.length() > 0 && !sb.toString().endsWith(COMMA)) {
						sb.append(COMMA_WITH_SPACE);
					}
					sb.append(v.trim());
				}
				String str = sb.toString();
				urlConnection.setRequestProperty(key, str);
				sb.setLength(0);
			}
			urlConnection.connect();

			Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
			int responseCode = urlConnection.getResponseCode();
			obj = RESTResponse.getInstance().setResponseCode(responseCode);

			if(responseCode >= 400){
				inputStream = urlConnection.getErrorStream();
				if (inputStream != null) {
					byte[] responseBytes = IOUtils.toByteArray(inputStream);
					obj.setResponseBody(responseBytes);
				}
			} else {
				inputStream = urlConnection.getInputStream();
				byte[] responseBytes = IOUtils.toByteArray(inputStream);
				obj.setResponseBody(responseBytes);
				
				for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
					String key = headerEntry.getKey();
					List<String> valueList = headerEntry.getValue();
					if (valueList != null) {
						for (String value : valueList) {
							obj.addHeader(key, value);
						}
					}
				}
			}

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (urlConnection != null) {
				try {
					if (urlConnection.getResponseCode() >= 400
							&& urlConnection.getErrorStream() != null) {
						urlConnection.getErrorStream().close();
					} else {
						if (urlConnection.getInputStream() != null) {
							urlConnection.getInputStream().close();
						}
					}
					urlConnection.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return obj;
	}

	private RESTResponse performHttpOperation(String method, String url, Map<String, List<String>> queryParams, String content,
			Map<String, List<String>> headers, int connectTimeout, int inputStreamReadTimeout) throws IOException{

		RESTResponse obj = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		DataOutputStream postStream = null;
		try {
			url = addQueryParamsToURL(url, queryParams);
			cat.debug("finally.. the last URL --- "+url);
			URL connectionUrl = new URL(url);
			urlConnection = (HttpURLConnection) connectionUrl.openConnection();
			urlConnection.setConnectTimeout(connectTimeout);
			urlConnection.setReadTimeout(inputStreamReadTimeout);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod(method);
			if (!headers.containsKey(CONTENT_TYPE)) {
				List<String> list = new ArrayList<String>();
				list.add(DEFAULT_CONTENT_TYPE);
				headers.put(CONTENT_TYPE, list);
			}
			StringBuilder sb = new StringBuilder();
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				for (String v : entry.getValue()) {
					if(v!=null&&v.length()>0){
						if (sb.length() > 0 && !sb.toString().endsWith(COMMA)) {
							sb.append(COMMA_WITH_SPACE);
						}
						sb.append(v.trim());
					}
				}
				urlConnection.setRequestProperty(key, sb.toString());
				sb.setLength(0);
			}

			postStream = new DataOutputStream(urlConnection.getOutputStream());
			postStream.writeBytes(content);
			postStream.flush();
			int responseCode = urlConnection.getResponseCode();
			obj = RESTResponse.getInstance().setResponseCode(responseCode);

			Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
			if(urlConnection.getResponseCode() >= 400){
				inputStream = urlConnection.getErrorStream();
				if (inputStream != null) {
					byte[] responseBytes = IOUtils.toByteArray(inputStream);
					obj.setResponseBody(responseBytes);
				}
			} else {
				inputStream = urlConnection.getInputStream();
				obj.setResponseMessage(urlConnection.getResponseMessage());
				byte[] responseBytes = IOUtils.toByteArray(inputStream);
				obj.setResponseBody(responseBytes);
			}
			
			for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
				String key = headerEntry.getKey();
				List<String> valueList = headerEntry.getValue();
				if (valueList != null) {
					for (String value : valueList) {
						obj.addHeader(key, value);
					}
				}
			}
		} finally {
			if (postStream != null) {
				try {
					postStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (urlConnection != null ) {
				try {
					if(urlConnection.getResponseCode()>=400 && urlConnection.getErrorStream()!=null){
						urlConnection.getErrorStream().close();
					}else{
						if(urlConnection.getInputStream()!=null){
							urlConnection.getInputStream().close();
						}
					}
					urlConnection.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return obj;
	}
	
	/**
	 * Executes a REST URL with POST 
	 * @param url
	 * @param content
	 * @param headers
	 * @param connectTimeout
	 * @param inputStreamReadTimeout
	 * @return the HTTP response
	 * @throws IOException
	 */
	public RESTResponse executePOST(String url, Map<String, List<String>> queryParams, String content,
			Map<String, List<String>> headers, int connectTimeout, int inputStreamReadTimeout) throws IOException {
			return performHttpOperation(POST, url, queryParams, content, headers, connectTimeout, inputStreamReadTimeout);
	}
	
	/**
	 * Executes a REST URL with PUT 
	 * @param url
	 * @param content
	 * @param headers
	 * @param connectTimeout
	 * @param inputStreamReadTimeout
	 * @return the HTTP response in form of
	 * @throws IOException
	 */
	public RESTResponse executePUT(String url, Map<String, List<String>> queryParams, String content,
			Map<String, List<String>> headers, int connectTimeout, int inputStreamReadTimeout) throws IOException {
			return performHttpOperation(PUT, url, queryParams, content, headers, connectTimeout, inputStreamReadTimeout);
	}
	
	public static String addQueryParamsToURL(String url, Map<String, List<String>> params) {
		StringBuilder rt = new StringBuilder(url);
		if (!url.endsWith(QUESTION_MARK)) {
			if(url.contains(QUESTION_MARK)){
				rt.append(AND_MARK);
			}else{
				rt.append(QUESTION_MARK);
			}
		}
		List<NameValuePair> nvp = new LinkedList<NameValuePair>();
		for (Map.Entry<String, List<String>> entry : params.entrySet()) {
			List<String> values = entry.getValue();
			for (String value : values) {
				nvp.add(new BasicNameValuePair(entry.getKey(), value));
			}
		}
		rt.append(URLEncodedUtils.format(nvp, DEFAULT_PARAM_ENCODING));
		return rt.toString();
	}

	/**
	 * Executes a REST URL to perform the delete operation.
	 * @param url
	 * @param params
	 * @param headers
	 * @param connectTimeout
	 * @param inputStreamReadTimeout
	 * @return the HTTP response in form of
	 * @throws IOException
	 */
	public RESTResponse executeDELETE(String url, Map<String, List<String>> params, Map<String, List<String>> headers, int connectTimeout, 
			int inputStreamReadTimeout)	throws IOException {
		
		RESTResponse obj = null;
		HttpURLConnection urlConnection = null;
		ByteArrayOutputStream outputStream = null;
		InputStream inputStream = null;

		try {
			URL connectionUrl = new URL(addQueryParamsToURL(url, params));
			urlConnection = (HttpURLConnection)connectionUrl.openConnection();
			urlConnection.setConnectTimeout(connectTimeout);
			urlConnection.setReadTimeout(inputStreamReadTimeout);
			urlConnection.setRequestMethod(DELETE);
			for (Map.Entry entry : headers.entrySet()) {
				String key = (String)entry.getKey();
				for (Object v : (List)entry.getValue()) {
					urlConnection.setRequestProperty(key, (String)v);
				}
			}
			urlConnection.connect();
			outputStream = new ByteArrayOutputStream();
			int responseCode = urlConnection.getResponseCode();

			obj = RESTResponse.getInstance().setResponseCode(responseCode);
			if (urlConnection.getResponseCode()>=400) {
				inputStream = urlConnection.getErrorStream();
				if (inputStream != null) {
					byte[] responseBytes = IOUtils.toByteArray(inputStream);
					obj.setResponseBody(responseBytes);
				}	
			} else {
				Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
				inputStream = urlConnection.getInputStream();
				obj.setResponseMessage(urlConnection.getResponseMessage());
				byte[] responseBytes = IOUtils.toByteArray(inputStream);
				obj.setResponseBody(responseBytes);
					
				for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
					String key = headerEntry.getKey();
					List<String> valueList = headerEntry.getValue();
					if (valueList != null) {
						for (String value : valueList) {
							obj.addHeader(key, value);
						}
					}
				}
			}
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (urlConnection != null) {
				try {
					if(urlConnection.getResponseCode() >= 400 && urlConnection.getErrorStream() != null){
						urlConnection.getErrorStream().close();
					} else {
						if (urlConnection.getInputStream() != null) {
							urlConnection.getInputStream().close();
						}
					}
					urlConnection.disconnect();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return obj;
	}
}