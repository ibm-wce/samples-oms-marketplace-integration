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
import com.yantra.shared.dbclasses.YFS_Order_HeaderDBHome;
import com.yantra.shared.dbi.YFS_Order_Header;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.ycp.agent.server.YCPAbstractAgent;
import com.yantra.ycp.core.YCPContext;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.ysc.util.YSCApiUtils;

/**
 * BTOrderSyncIntegration
 */
public class BTOrderSyncIntegration  extends YCPAbstractAgent implements BTConstantsLiterals{
	
	private static YFCLogCategory cat = YFCLogCategory.instance(BTOrderSyncIntegration.class.getName());
	
	public List<Document> getJobs(YFSEnvironment env, Document criteria, Document lastMessageCreated) throws Exception {
		YFSContext oCtx = (YFSContext)env;
		YFCDocument critDoc = YFCDocument.getDocumentFor(criteria);
		String entCode = critDoc.getDocumentElement().getAttribute("EnterpriseCode");
		String node = critDoc.getDocumentElement().getAttribute("ShipNode");
		List<Document> jobList = new ArrayList<Document>();
		MarketPlaceConnector mpc = MarketPlaceRegistry.getInstance().getMarketPlaceConnector();
		//Call REST API to fetch Orders
		cat.debug("Start testing Fetch Order ");
		Map<String,String> qp = new HashMap<String,String>();
		qp.put("fulfillment_status_id", "processing");
		String xmlResp = (String)mpc.fetchOrders(qp, null);
		cat.debug("End testing Fetch Order  ");
		YFCDocument jdoc = YFCDocument.getDocumentFor(xmlResp);
		//Transform XML to createOrder/changeOrder input
		for(YFCElement jelem : jdoc.getDocumentElement().getChildren("data")){
			YFCElement jcelem = jelem.getChildElement("Customer");
			YFCElement jchelem = jelem.getChildElement("Channel");
			YFCElement jsaelem = jelem.getChildElement("ShippingAddress");
			YFCElement joelem = jelem.getChildElement("Order");
			YFCIterable<YFCElement> iter = jelem.getChildren("ItemTitle"); 
			String oid = joelem.getChildElement("id").getNodeValue();
			String orderNo = BT_PREF + oid;
			String docType = "0001";
			if(!iter.hasNext()){
				cat.warn("BT Order "+oid+" has no lines. Not synced to OM.");
				continue;
			}
			YFCElement oelem = YFCDocument.createDocument("Order").getDocumentElement();
			oelem.setAttribute("OrderNo", orderNo);
			oelem.setAttribute("EnterpriseCode", entCode);
			oelem.setAttribute("DocumentType", docType);
			YFS_Order_Header oOrderHeader = YFS_Order_HeaderDBHome.getInstance().selectWithEnterpriseKeyAndOrderNo(oCtx, entCode, orderNo);
			if(oOrderHeader==null){
				oelem.setAttribute("API", "createOrder");
				setAttribute(jchelem, oelem, "id", "EntryType", BT_PREF);
				setAttribute(jsaelem, oelem, "id", "BillToID", BT_PREF);
				if(!setDateAttribute(joelem, oelem, "created_on", "OrderDate"))
					setDateAttribute(joelem, oelem, "created", "OrderDate");
				oelem.setAttribute("SellerOrganizationCode", entCode);
				oelem.setAttribute("DraftOrderFlag", "N");
				YFCElement ollelem = oelem.createChild("OrderLines");
				for(int i=1;iter.hasNext();i++){
					YFCElement itelem = iter.next();
					YFCElement itoelem = itelem.getChildElement("ItemTitlesOrder");
					YFCElement skuelem = itelem.getChildElement("Sku");
					YFCElement olelem = ollelem.createChild("OrderLine");
					if(node!=null) olelem.setAttribute("ShipNode", node);
					olelem.setAttribute("PrimeLineNo", i);
					olelem.setAttribute("SubLineNo", 1);
					setAttribute(itoelem, olelem, "quantity", "OrderedQty");
					YFCElement olielem = olelem.createChild("Item");
					setAttribute(skuelem, olielem, "custom_code", "ItemID");
					olielem.setAttribute("UnitOfMeasure", "EACH");
				}
				YFCElement pistelem = oelem.createChild("PersonInfoBillTo");
				setAttribute(jsaelem, pistelem, "id", "PersonID", BT_PREF);
				setAttribute(jsaelem, pistelem, "zip", "ZipCode");
				setAttribute(jsaelem, pistelem, "phone", "MobilePhone");
				setAttribute(jsaelem, pistelem, "address_line1", "AddressLine1");
				setAttribute(jsaelem, pistelem, "address_line2", "AddressLine2");
				setAttribute(jsaelem, pistelem, "address_line3", "AddressLine3");
				setAttribute(jsaelem, pistelem, "address_name", "AddressID");
				setAttribute(jsaelem, pistelem, "state", "State");
				setAttribute(jsaelem, pistelem, "country", "Country");
				setAttribute(jsaelem, pistelem, "city", "City");
				setAttribute(jcelem, pistelem, "first_name", "FirstName");
				setAttribute(jcelem, pistelem, "last_name", "LastName");
				setAttribute(jcelem, pistelem, "email", "EMailID");
				setAttribute(jcelem, pistelem, "salutation", "Title");
				setAttribute(jcelem, pistelem, "company_name", "Company");
				cat.debug(oelem);
				jobList.add(oelem.getOwnerDocument().getDocument());
			} else{
				oelem.setAttribute("API", "changeOrder");
			}
		}
		cat.debug("Start testing Fetch Order ");
		qp = new HashMap<String,String>();
		qp.put("financial_status_id", "cancelled");
		xmlResp = (String)mpc.fetchOrders(qp, null);
		cat.debug("End testing Fetch Order  ");
		return jobList;
	}

	public void executeJob(YFSEnvironment env, Document executionMessage) throws Exception {
		YFCElement jobElem = YFCDocument.getDocumentFor(executionMessage).getDocumentElement();
		String api = jobElem.getAttribute("API");
		jobElem.removeAttribute("API");		
		YSCApiUtils.invokeAPI(env, api, jobElem.getOwnerDocument(), false);
		if("createOrder".equals(api)){
			String custId = jobElem.getAttribute("BillToID");
			String org = jobElem.getAttribute("EnterpriseCode");
			YFCElement jobElem2 = YFCDocument.createDocument("Customer").getDocumentElement();
			jobElem2.setAttribute("CustomerID", custId);
			jobElem2.setAttribute("CustomerType", "02");
			jobElem2.setAttribute("OrganizationCode", org);
			jobElem2.createChild("Consumer").createChild("BillingPersonInfo").setAttribute("PersonID", custId);
			YSCApiUtils.invokeAPI(env, "createCustomer", jobElem2.getOwnerDocument(), false);
		}
	}
	
	public static boolean setAttribute(YFCElement src, YFCElement dest, String srcAtt, String destAttr){
		return setAttribute(src, dest, srcAtt, destAttr, null);
	}
	
	public static boolean setAttribute(YFCElement src, YFCElement dest, String srcAtt, String destAttr, String prefix){
		String val = src.getChildElement(srcAtt).getNodeValue();
		if(val !=null && !"null".equals(val)){
			if(prefix!=null) val = prefix + val;
			dest.setAttribute(destAttr, val);
			return true;
		}
		return false;
	}
	
	public static boolean setDateAttribute(YFCElement src, YFCElement dest, String srcAtt, String destAttr){
		String val = src.getChildElement(srcAtt).getNodeValue();
		if(val !=null && !"null".equals(val)){
			dest.setDateTimeAttribute(destAttr, new YTimestamp(val, BT_DATE_FORMAT, false));
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		YCPContext oEnv = null;
		try{
			oEnv = new YCPContext("admin", "test");
			BTOrderSyncIntegration cdeAgent = new BTOrderSyncIntegration();
			YFCDocument ac = YFCDocument.getDocumentFor("<Attributes EnterpriseCode=\"MATRIX\" />");
			List<Document> list = cdeAgent.getJobs(oEnv, ac.getDocument(), null);
			for(Document doc : list) cdeAgent.executeJob(oEnv, doc);
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			oEnv.rollback();
			System.exit(0);
		}
	}
}