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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.yantra.yfc.log.YFCLogCategory;

/**
 */
public class RESTRequest implements RESTConstants {
	private static YFCLogCategory cat = YFCLogCategory.instance(RESTRequest.class.getName());
	
	private String url;
	private Map<String, List<String>> queryParams = new LinkedHashMap<String, List<String>>();
	private Map<String, List<String>> requestHeaders = new LinkedHashMap<String, List<String>>();
	private String httpMethod = null;
	private String postContent = null;
	
	/**
	 * Sets the base URL of the HTTP request.
	 * @param url
	 * @return the rest handler.
	 */
	public RESTRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * Adds a parameter to the HTTP request.
	 * @param key
	 * @param value
	 * @return
	 */
	public RESTRequest addParam(String key, String value) {
		if (!queryParams.containsKey(key)) {
			queryParams.put(key, new ArrayList<String>());
		}
		queryParams.get(key).add(value);
		return this;
	}

	/**
	 * Adds a HTTP header to the HTTP request.
	 * @param key
	 * @param value
	 * @return
	 */
	public RESTRequest addHeader(String key, String value) {
		if (!requestHeaders.containsKey(key)) {
			requestHeaders.put(key, new ArrayList<String>());
		}
		requestHeaders.get(key).add(value);
		return this;
	}
	
	/**
	 * Get a parameter value list.
	 * @param key the parameter key.
	 * @return the parameter value list.
	 */
	public List<String> getParam(String key) {
		return queryParams.get(key);
	}

	/**
	 * Get a header value list.
	 * @param key the header key.
	 * @return the header value list.
	 */
	public List<String> getHeader(String key) {
		return requestHeaders.get(key);
	}
		
	/**
	 * Executes a REST request.
	 * @return the response object
	 * @throws RuntimeException
	 */
	public RESTResponse execute() throws RuntimeException {
		
		final String METHODNAME = "execute()";
		RESTResponse responseObj = null;
		cat.debug("the url is..."+url+" method is..."+httpMethod);
		
		try {
			if (httpMethod ==  null || httpMethod.equals(GET)) {
				responseObj = RESTURLConnection.getInstance().executeGET(url,queryParams, requestHeaders, DEFAULT_TIME_OUT, DEFAULT_TIME_OUT);
			} else if (httpMethod.equals(PUT)) {
				responseObj = RESTURLConnection.getInstance().executePUT(url, queryParams, postContent, requestHeaders, DEFAULT_TIME_OUT, DEFAULT_TIME_OUT);
			} else if (httpMethod.equals(POST)) {
				responseObj =  RESTURLConnection.getInstance().executePOST(url,queryParams, postContent, requestHeaders, DEFAULT_TIME_OUT, DEFAULT_TIME_OUT);
			} else if (httpMethod.equals(DELETE)) {
				responseObj = RESTURLConnection.getInstance().executeDELETE(url,queryParams, requestHeaders, DEFAULT_TIME_OUT, DEFAULT_TIME_OUT);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return responseObj;
	}
	
	/**
	 * @return The base URL.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @param post
	 */
	public void setPost(boolean post) {
		if (post) {
			httpMethod = POST;
		}
	}

	/**
	 * Sets the HTTP operation type as PUT.
	 * @param put true to set the HTTP operation type as PUT
	 */
	public void setPut(boolean put) {
		if (put) {
			httpMethod = PUT;
		}
	}
	
	/**
	 * @param content
	 */
	public void setContent(String content) {
		postContent = content;
	}
}