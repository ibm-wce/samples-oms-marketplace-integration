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

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;

public class MarketPlaceRegistry implements Registry {
	private static YFCLogCategory cat = YFCLogCategory.instance(MarketPlaceRegistry.class.getName());
	private static MarketPlaceRegistry singleton = new MarketPlaceRegistry();
	private static boolean initialized = false;
	private MarketPlaceConnector marketPlaceConnectorInstance = null;
	private static String marketPlaceConnectorClassName = null;
	
	private MarketPlaceRegistry(){
		super();
	}
	
	public static MarketPlaceRegistry getInstance() {
		if (!initialized) {
			try {
				singleton.initializeRegistry();
			} catch (Exception e) {
				throw new YFCException(e);
			}
		}
		return singleton;
	}

	private void initializeRegistry(){
		synchronized (MarketPlaceRegistry.class) {
			if(!initialized){
				try {
					marketPlaceConnectorClassName = YFSSystem.getProperty("integration.marketplace.class");
					cat.debug("Market place class: "+marketPlaceConnectorClassName);
					marketPlaceConnectorInstance = createMarketPlaceConnector(marketPlaceConnectorClassName);
					initialized = true;
				} catch(Exception ex){
					YFCException e = new YFCException(ex);
					e.setErrorDescription("Property yfs.integration.marketplace.class not set.");
					throw new YFCException(e);
				}
			}
		}
	}
	
	public MarketPlaceConnector getMarketPlaceConnector(){
		return marketPlaceConnectorInstance;
	}
	
	private static MarketPlaceConnector createMarketPlaceConnector(String marketPlaceConnectorClassName){
		MarketPlaceConnector connectorInstance = null;
		try {
			Class<?> connectorClassz = Class.forName(marketPlaceConnectorClassName);
			connectorInstance = (MarketPlaceConnector) connectorClassz.newInstance();
		} catch (Exception e) {
			throw new YFCException(e);
		}
		return connectorInstance;
	}
	
	@Override
	public void refresh() throws Exception {
		synchronized (MarketPlaceRegistry.class) {
			if(initialized){
				singleton = new MarketPlaceRegistry();
				singleton.initializeRegistry();
			}	
		}
	}
}