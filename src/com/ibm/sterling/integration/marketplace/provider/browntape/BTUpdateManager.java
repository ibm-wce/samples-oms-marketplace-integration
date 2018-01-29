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

import org.w3c.dom.Document;

import com.ibm.sterling.integration.marketplace.core.MarketPlaceConnector;
import com.ibm.sterling.integration.marketplace.core.MarketPlaceRegistry;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * BTUpdateManager
 */
public class BTUpdateManager implements BTConstantsLiterals{

	private static YFCLogCategory cat = YFCLogCategory.instance(BTUpdateManager.class.getName());
	
	public Document updateOrder(YFSEnvironment env, Document doc) throws Exception {
		try {
			cat.beginTimer("updateOrder");
			if(doc==null) return doc;
			YFCElement inElem = YFCDocument.getDocumentFor(doc).getDocumentElement();
			String entryType = inElem.getAttribute("EntryType");
			if(YFCCommon.isVoid(entryType) || !entryType.startsWith(BT_PREF)) return doc;
			String status = null;
			for(YFCElement elem : inElem.getElementsByTagName("ToOrderReleaseStatus")){
				String thisStatus = elem.getAttribute("Status");
				if(!YFCCommon.isVoid(status) && !YFCCommon.isVoid(thisStatus) && !thisStatus.equals(status)) return doc;
				status = thisStatus;
			}
			String orderNo = inElem.getAttribute("OrderNo");
			String xmlResp = updateOrder(status, orderNo);
			if(xmlResp!=null){
				YFCElement respElem = YFCDocument.getDocumentFor(xmlResp).getDocumentElement();
				if("ORDERS".equals(respElem.getNodeName()) && 
						(YFCCommon.isVoid(respElem.getChildElement("success")) || !"true".equals(respElem.getChildElement("success").getNodeValue())))
					throw new Exception("Update failed on Browntape!!! \n"+xmlResp);
			} else
				throw new Exception("Update failed on Browntape!!!");
			return doc;
		} finally {
			cat.endTimer("updateOrder");
		}
	}

	public static String updateOrder(String status, String orderNo) {
		String resp = "<NA/>";
		if(status==null || orderNo==null || !orderNo.startsWith(BT_PREF)) return null;
		String btstatus = null;
		switch(status) {
			case "1100"		:	btstatus = "order accepted"; 	break;
			case "3200"		:	btstatus = "ready to ship"; 	break;
			case "3200.01"	:	btstatus = "ready to ship"; 	break;
			case "3700"		:	btstatus = "shipped"; 			break;
			default			:	return resp;
		}
		Map<String,String> order = new HashMap<String,String>();
		order.put("order_id", orderNo.substring(BT_PREF.length()));
		order.put("fulfillment_status", btstatus);
		return (String)MarketPlaceRegistry.getInstance().getMarketPlaceConnector().updateOrder(order, null);
	}
	
	public Document updateAvailability(YFSEnvironment env, Document doc) throws Exception{
		try {
			cat.beginTimer("updateAvailability");
			if(doc==null) return null;
			YFCElement inElem = YFCDocument.getDocumentFor(doc).getDocumentElement();
			String itemid = inElem.getAttribute("ItemID");
			YFCElement acElem = inElem.getChildElement("AvailabilityChanges").getChildElement("AvailabilityChange");
			if(acElem==null) return doc;
			String availableQty = acElem.getAttribute("OnhandAvailableQuantity");
			String alertlevel = acElem.getAttribute("AlertLevel");
			String xmlResp = updateAvailability(itemid, availableQty, alertlevel);
			if(xmlResp!=null) {
				YFCElement respElem = YFCDocument.getDocumentFor(xmlResp).getDocumentElement();
				if(YFCCommon.isVoid(respElem.getChildElement("success")) || 
						!"true".equals(respElem.getChildElement("success").getNodeValue()))
					throw new Exception("Update failed on Browntape!!! Response-\n"+xmlResp);
			} else
				throw new Exception("Update failed on Browntape!!!");
			return doc;
		} finally {
			cat.endTimer("updateAvailability");
		}
	}

	public static String updateAvailability(String itemid, String availableQty, String alertlevel) {
		if(itemid==null || availableQty==null || alertlevel==null) return null;
		String group = YFSSystem.getProperty("integration.marketplace."+BT_PROVIDER+".invallocpercent.grouping.foritem."+itemid);
		if(YFCCommon.isVoid(group)) group = "default";
		String invallocpercent = YFSSystem.getProperty("integration.marketplace."+BT_PROVIDER+".invallocpercent."+group+".forlevel."+alertlevel);
		if(YFCCommon.isVoid(invallocpercent)) invallocpercent = YFSSystem.getProperty("integration.marketplace."+BT_PROVIDER+".invallocpercent."+group);
		if(YFCCommon.isVoid(invallocpercent)) invallocpercent = "100";
		double invallocpc = Double.parseDouble(invallocpercent);
		if(invallocpc<0d || invallocpc>100d) return null;
		double avQty = Double.parseDouble(availableQty);
		double provQty = (invallocpc * avQty)/100;
		Map<String,String> xyz = new HashMap<String,String>();
		xyz.put("custom_code", itemid);
		xyz.put("stock", provQty+"");
		List<Map<String,String>> skusList = new ArrayList<Map<String,String>>(1);
		skusList.add(xyz);
		return (String)MarketPlaceRegistry.getInstance().getMarketPlaceConnector().updateInventoryAvailability(skusList, null);
	}
}