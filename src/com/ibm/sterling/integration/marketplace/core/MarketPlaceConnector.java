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

package com.ibm.sterling.integration.marketplace.core;

import java.util.List;
import java.util.Map;
import com.yantra.yfc.util.YFCException;

/**
 * Connector interface which connects OMS system with Market Place
 */
public interface MarketPlaceConnector {
	
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON_SHORT_FORMAT = "json";
	public static final String APPLICATION_XML_SHORT_FORMAT = "xml";
	
	/**
	 * This method fetches orders from market place based on selectionCriteria
	 * object passed. SelectionCriteria can be used to pass search parameters
	 * like orderCreateDate, orderStatus, orderId, customerId etc.,
	 * 
	 * @param parameters
	 * @param responseFormat
	 * @return String - Response
	 * @throws YFCException
	 */	
	public Object fetchOrders(Map selectionCriteria, String responseFormat) throws YFCException;
	
	/**
	 * This method updates orders present in market place.
	 * @param ordersList
	 *            - List of HashMap objects. Each hashMap contains details about
	 *            order which needs to be updated.
	 * @param responseFormat
	 * @return String - Response
	 * @throws YFCException
	 */
	public Object updateOrders(List<Map> ordersList, String responseFormat)	throws YFCException;

	/**
	 * This method updates a single order.
	 * @param order - Map containing details about order which needs to be updated.
	 * @param responseFormat
	 * @return String - Response
	 * @throws YFCException
	 */
	public Object updateOrder(Map order, String responseFormat) throws YFCException;
	
	/**
	 * This method creates order in market place
	 * @param order
	 * @param responseFormat
	 * @return
	 * @throws YFCException
	 */
	public Object addOrder(Map order, String responseFormat) throws YFCException;
	
	/**
	 * This method fetches inventory availability for a given selectionCriteria.
	 * 
	 * @param skuIds- Map of selection criteria parameters (Ex:sku_code, created_from, created_to, modified_from, modified_to)
	 * @param responseFormat
	 * @return String - Response
	 * @throws YFCException
	 */
	public Object fetchInventoryAvailability(Map selectionCriteria, String responseFormat) throws YFCException;
	
	/**
	 * This method updates inventory availability for a given list of skuIds.
	 * 
	 * @param skuList
	 *            - List of HashMap object. Each Map contains skuId,inventoryAvailability and additional parameters required
	 *            for inventory processing (like adjustPending Stock, update different market place with different inventory count etc.,)
	 * @param responseFormat
	 * @return String - Response
	 * @throws YFCException 
	 */
	public Object updateInventoryAvailability(List<Map<String,String>> skuList, String responseFormat) throws YFCException;
}