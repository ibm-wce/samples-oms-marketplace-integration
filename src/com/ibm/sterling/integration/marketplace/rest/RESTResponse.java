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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The RESTResponse class is the execution result of a HTTP request.
 */
public class RESTResponse {
	
    private RESTResponse() { }

    public static RESTResponse getInstance(){
        return new RESTResponse();
    }

    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    byte[] responseBody;
    int responseCode;
    String responseMessage;

    /**
     * Get a header of the HTTP response with specified header key
     * @param key
     * @return header value list
     */
    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Get header list of the HTTP response
     * @return the header list
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public RESTResponse addHeader(String key, String value) {
    	if(!headers.containsKey(key)){
    		headers.put(key, new ArrayList<String>());
    	}
    	headers.get(key).add(value);
        return this;
    }
    
    public RESTResponse setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    /**
     * Get the response code of the HTTP response
     * @return response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    public RESTResponse setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    /**
     * Get the HTTP response body as string
     * @return response body
     */
    public String getResponseAsString(){
        return new String(responseBody);
    }

    /**
     * Get the response message.
     * @return
     */
    public String getResponseMessage() {
		return responseMessage;
	}

    /**
     * Set the response message.
     * @param responseMessage
     */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
}