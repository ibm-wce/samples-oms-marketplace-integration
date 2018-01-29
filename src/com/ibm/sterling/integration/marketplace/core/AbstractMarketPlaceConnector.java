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

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import com.yantra.yfs.core.YFSSystem;

/**
 * Abstract market place connector class which provides utility methods like
 * fetching end point URL, converting JSON to XML.
 *
 */
public abstract class AbstractMarketPlaceConnector implements MarketPlaceConnector {
	
	/**
	 * Fetches end point URL configured in market place properties file based on
	 * the provider, component and action parameters. It looks for key
	 * <code>integration.marketplace."+provider+".endpoint."+component+"."+action</code>
	 * in properties file and returns the value
	 * associated with this key.
	 * 
	 * @param provider - String - Market Place provider - Ex: BrownTape	 
	 * @param component - String - Type of object -- Order, Inventory, Price
	 * @param action - String - Operation to be performed on the component - Get, Process, Sync, Change etc.,
	 * @return - EndPoint URL defined in the properties file.
	 */
	public String getEndPointURL(String provider, String component, String action){
		return getEndPointURL(provider, component, action, null);
	}
	
	/**
	 * Fetches end point URL configured in market place properties file based on
	 * the provider, component, action, attribute parameters. It looks for key
	 * <code>integration.marketplace."+provider+".endpoint."+component+"."+action</code> in properties
	 * file and returns the value
	 * associated with this key.
	 * 
	 * @param provider - String - Market Place provider - Ex: BrownTape	 
	 * @param component - String - Type of object -- Order, Inventory, Price
	 * @param action - String - Operation to be performed on the component - Get, Process, Sync, Change etc.,
	 * @param attribute - String - Type of action - Ex:, create, delete etc.,
	 * @return - EndPoint URL defined in the properties file.
	 */
	public String getEndPointURL(String provider, String component, String action, String attribute){
		String key = "integration.marketplace."+provider+".endpoint."+component+"."+action;
		if(attribute!=null)
			key += "."+attribute;
		String endPointURL = YFSSystem.getProperty(key);
		return endPointURL;
	}
	
	/**
	 * Returns XML object representation of the JSON Object passed in.
	 * @param object - JSONObject
	 * @param rootNode - RootNode of the returned XML 
	 * @return - XML Representation of the JSONObject
	 */
	public String getXML(Object object, String rootNode){
		
		StringBuilder sb = new StringBuilder();
		if(rootNode !=  null && rootNode.length() > 0){
			sb.append('<').append(rootNode).append('>');
		}
		
		if(object != null){
			if(object instanceof JSONObject){
				JSONObject jsonObj = (JSONObject)object;
				Iterator<Map.Entry<String, Object>> iterator = jsonObj.entrySet().iterator();
				while(iterator.hasNext()){
					Map.Entry<String,Object> nextElement = iterator.next();
					String key = nextElement.getKey();
					Object val = nextElement.getValue();
					if(val instanceof JSONObject){
						sb.append(getXML(val,key)).toString();
					} else if(val instanceof JSONArray){
						JSONArray jsonArray = (JSONArray)val;
						for (Object jsonArrayElement : jsonArray) {
							sb.append('<').append(key).append('>');
							sb.append(getXML(jsonArrayElement, null));
							sb.append("</").append(key).append(">");
						}
					} else {
						sb.append('<').append(key).append('>').append(val).append("</").append(key).append(">");
					}
				}
			}
		}
		if(rootNode !=  null && rootNode.length() > 0){
			sb.append("</").append(rootNode).append(">");
		}
		return sb.toString();
	}
}