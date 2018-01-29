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

/**
 * Class used to propagate remote call exceptions
 */
public class MarketPlaceException extends Exception{
	
	/**
	 * The response code.
	 */
	private int responseCode = 0;
	
	/**
	 * Constructor
	 * @param message the error message.
	 * @param setResponseCode the error response code.
	 */
	public MarketPlaceException(String message, int setResponseCode) {
		super(message);
		responseCode = setResponseCode;
	}
	
	/**
	 * Constructor
	 * @param message the error message.
	 * @param setResponseCode the error response code.
	 * @param cause the error cause.
	 */
	public MarketPlaceException(String message, int setResponseCode, Throwable cause) {
		super(message, cause);
		responseCode = setResponseCode;
	}
	
	/**
	 * Get the response code.
	 * @return the response code.
	 */
	public int getResponseCode() {
		return responseCode;
	}
}