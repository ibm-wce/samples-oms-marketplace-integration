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

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.json.JSON;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

import com.ibm.sterling.integration.marketplace.core.MarketPlaceException;
import com.yantra.yfc.log.YFCLogCategory;

public class RestRequestHelper implements RESTConstants {

	private static YFCLogCategory cat = YFCLogCategory.instance(RestRequestHelper.class.getName());
	private static RestRequestHelper iRestHandlerHelper = new RestRequestHelper();

	private RestRequestHelper() { }

	public static RestRequestHelper getInstance() {
		return iRestHandlerHelper;
	}

	public JSONObject issueRESTRequest(String astrURL,
			Map<String, String> amapQueryParameters, String postContent, Map<String,String> headers,
			int expectedReturnCode) throws MarketPlaceException, JSONException {
				
		RESTRequest restRequest = new RESTRequest();
		restRequest.setUrl(astrURL);
		
		cat.debug("the URL is......"+astrURL);
		if (postContent != null && postContent.length() > 0 ){
			restRequest.setPost(true);
			restRequest.setContent(postContent);
			String contentType = DEFAULT_CONTENT_TYPE;
			if(headers != null && headers.containsKey(CONTENT_TYPE)){
				contentType = headers.get(CONTENT_TYPE);
			} 
			restRequest.addHeader(CONTENT_TYPE, contentType);
		}

		if (amapQueryParameters != null) {
			// adding URL query string parameters
			Iterator<String> itrParameterNames = amapQueryParameters.keySet()
					.iterator();
			while (itrParameterNames.hasNext()) {
				String strParameterName = itrParameterNames.next();
				String strParameterValue = amapQueryParameters
						.get(strParameterName);
				restRequest.addParam(strParameterName, strParameterValue);
				cat.debug("Added these query parameters.."+strParameterName+" == "+strParameterValue);
			}
		}

		// Do the REST request
		cat.debug("the final request URL...."+restRequest.getUrl());
		RESTResponse res = restRequest.execute();
		handleError(res, expectedReturnCode);
		JSONObject json = null;
		String strResponse = res.getResponseAsString();
		cat.debug("the string response is.............."+strResponse);
		if(strResponse.startsWith("<?xml")){
			JSON.parse(strResponse);
		}
		else if (strResponse != null) {
			json = new JSONObject(strResponse);
		}
		return json;
	}

	/**
	 * This method checks if the return code of the request was a success. If
	 * not, throws an exception.
	 * 
	 * @param json
	 *            The Json message
	 * @throws MarketPlaceException
	 *             if the request failed.
	 */
	public void handleError(RESTResponse responseObject, int expectedCode) throws MarketPlaceException {

		int responseCode = responseObject.getResponseCode();
		if (responseCode != expectedCode) {
			String responseAsString = responseObject.getResponseAsString();
			cat.debug("response string in error handler == "+responseCode);
			MarketPlaceException marketPlaceException = new MarketPlaceException (responseAsString, responseCode);
			throw marketPlaceException;
		}
	}
}