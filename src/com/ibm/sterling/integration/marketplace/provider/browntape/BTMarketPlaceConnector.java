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

package com.ibm.sterling.integration.marketplace.provider.browntape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.apache.http.HttpStatus;

import com.ibm.sterling.integration.marketplace.core.AbstractMarketPlaceConnector;
import com.ibm.sterling.integration.marketplace.rest.RestRequestHelper;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;

public class BTMarketPlaceConnector extends AbstractMarketPlaceConnector implements BTConstantsLiterals{
	private static YFCLogCategory cat = YFCLogCategory.instance(BTMarketPlaceConnector.class.getName());
	private static Map<String,String> authMap = new HashMap<String,String>();
	private static int EXPECTED_STATUS_OK = HttpStatus.SC_OK;
	
	static {
		authMap.put("username", YFSSystem.getProperty("integration.marketplace.browntape.username"));
		authMap.put("auth_string", YFSSystem.getProperty("integration.marketplace.browntape.authstring"));
	}
	
	private static Map<String, String> getAuthMap(){
		return authMap;
	}
	
	@Override
	public Object fetchOrders(Map selectionCriteria, String responseFormat) throws YFCException {
		String endPointURL = getEndPointURL(BT_PROVIDER, "order", "get");
		try {	
			if(selectionCriteria == null){
				selectionCriteria = new HashMap<String,String>();
			}
			selectionCriteria.putAll(getAuthMap());
			JSONObject response = RestRequestHelper.getInstance().issueRESTRequest(endPointURL, selectionCriteria, null, null,EXPECTED_STATUS_OK);
			cat.debug("the response from market place is:  "+response);
			if(responseFormat == null || responseFormat.equals(APPLICATION_XML) || responseFormat.equals(APPLICATION_XML_SHORT_FORMAT)){
				return super.getXML(response, "ORDERS");
			} else if(responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT)){
				return response;
			}
		} catch (Exception e) {
			throw new YFCException(e);
		}
		return null;
	}

	@Override
	public Object updateOrder(Map order, String responseFormat) throws YFCException {
		String endPointURL = super.getEndPointURL(BT_PROVIDER, "order", "update");
		Map<String, String> queryParams = getAuthMap();
		try {			
			if(order.get("order_id") != null){
				order.putAll(queryParams);
				JSONObject response = RestRequestHelper.getInstance().issueRESTRequest(endPointURL, order, null, null, EXPECTED_STATUS_OK);
				cat.debug("the response from market place is:  "+response);
				if(responseFormat == null || responseFormat.equals(APPLICATION_XML) || responseFormat.equals(APPLICATION_XML_SHORT_FORMAT)){
					return super.getXML(response, "ORDERS");
				} else if(responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT)){
					return response;
				}
			} else {
				cat.debug("order_id is NOT present. No updates will be done ");
			}
		} catch (Exception e) {
			throw new YFCException(e);
		}
		return null;
	}

	@Override
	public Object fetchInventoryAvailability(Map selectionCriteria, String responseFormat) throws YFCException {
		String endPointURL = super.getEndPointURL(BT_PROVIDER, "inv", "get");
		if(selectionCriteria == null){
			selectionCriteria = new HashMap<String,String>();
		}
		selectionCriteria.putAll(getAuthMap());
		try {			
			JSONObject response = RestRequestHelper.getInstance().issueRESTRequest(endPointURL, selectionCriteria, null, null, EXPECTED_STATUS_OK);
			cat.debug("the response from market place is:  "+response);
			if(responseFormat == null || responseFormat.equals(APPLICATION_XML) || responseFormat.equals(APPLICATION_XML_SHORT_FORMAT)){
				return super.getXML(response, "SKU");
			} else if(responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT)){
				return response;
			}
		} catch (Exception e) {
			throw new YFCException(e);
		}
		return null;
	}

	@Override
	public Object updateInventoryAvailability(List<Map<String, String>> skuList, String responseFormat)
			throws YFCException {
		String endPointURL = super.getEndPointURL(BT_PROVIDER, "inv", "update");
		Map<String, String> queryParams = getAuthMap();
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		JSONArray postContent;
		try {
			postContent = new JSONArray(skuList);
		} catch (JSONException e1) {
			YFCException ex = new YFCException(e1);
			ex.setAttribute("ResponseCode", HttpStatus.SC_BAD_REQUEST+"");
			throw new YFCException(ex);
		}
		String postContentStr = "data="+postContent.toString();
		cat.debug("the post content for update inventory is: "+postContentStr);
		try {			
			JSONObject response = RestRequestHelper.getInstance().issueRESTRequest(endPointURL, queryParams, postContentStr, headers, EXPECTED_STATUS_OK);
			cat.debug("the response from market place is:  "+response);
			if(responseFormat == null || responseFormat.equals(APPLICATION_XML) || responseFormat.equals(APPLICATION_XML_SHORT_FORMAT)){
				return super.getXML(response, "INVENTORY");
			} else if(responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT)){
				return response;
			}
		} catch (Exception e) {
			throw new YFCException(e);
		}
		return null;
	}

	@Override
	public Object updateOrders(List<Map> ordersList, String responseFormat) throws YFCException {
		//By default this method doesn't do anything.
		return null;
	}

	@Override
	public Object addOrder(Map order, String responseFormat) throws YFCException {
		
		final String methodName = "addOrder(Map, String)";
		String endPointURL = getEndPointURL(BT_PROVIDER, "order", "change", "create");
		Map<String, String> queryParams = getAuthMap();
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		JSONArray postContent;
		List orderList = new ArrayList(1);
		orderList.add(order);
		try {
			postContent = new JSONArray(orderList);
		} catch (JSONException e) {
			throw new YFCException(e);
		}
		
		String postContentStr = "data="+postContent.toString();
		try {			
			JSONObject response = RestRequestHelper.getInstance().issueRESTRequest(endPointURL, queryParams, postContentStr, headers, EXPECTED_STATUS_OK);
			if(responseFormat == null || (responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT))){
				return response;
			} else if(responseFormat == null || (responseFormat.equals(APPLICATION_JSON) || responseFormat.equals(APPLICATION_JSON_SHORT_FORMAT))){
				String xmlResponse = super.getXML(response, "ORDER");
				return xmlResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}