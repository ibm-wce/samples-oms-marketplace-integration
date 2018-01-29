[comment]: # (This file has been furnished by IBM as a simple example to provide an illustration. These examples have not)
[comment]: # (been thoroughly tested under all conditions. IBM, therefore, cannot guarantee reliability, serviceability )
[comment]: # (or function of these programs. All programs contained herein are provided to you "AS IS". )

[comment]: # (This file may include the names of individuals, companies, brands and products in order to illustrate them )
[comment]: # (as completely as possible. All of these names are ficticious and any similarity to the names and addresses )
[comment]: # (used by actual persons or business enterprises is entirely coincidental. )


# IBM Order Management on Cloud : Browntape Marketplace Integration Asset
<br/>

## Table of contents

- [Overview](#overview)
- [Getting started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Downloading the asset](#downloading-the-asset)
    - [Working with existing customizations](#working-with-existing-customizations)
- [Setting up Browntape](#setting-up-browntape)
- [Setting up IBM Order Management](#setting-up-ibm-order-management)
    - [Configuring custom properties](#configuring-custom-properties)
    - [Configuring channels](#configuring-channels)
    - [Configuring participant model](#configuring-participant-model)
    - [Configuring inventory and sourcing](#configuring-inventory-and-sourcing)
    - [Configuring items](#configuring-items)
    - [Configuring RTAM agent](#configuring-rtam-agent)
    - [Configuring order sync agent](#configuring-order-sync-agent)
    - [Configuring change order](#configuring-change-order)
    - [Extending custom implementation](#extending-custom-implementation)
- [Disclaimer](#disclaimer)
<br/>    
    
## Overview

IBM Order Management provides order orchestration through a centralized inventory, order promising and fulfillment hub to support omni-channel fulfillment. It collects orders from multiple order capture channels and provides a single source of order status information and single view of supply and demand across channels. 
Order Management system is integrated with WebSphere Commerce online store, call center and store allowing businesses to capture orders through online, physical stores or call center agents.
<br/> <br/>
But most of the Tier 1/2, SMB in emerging markets like India are constantly trying to reach out and capture new shoppers by leveraging prominent market place players like Amazon, FlipKart, SnapDeal. Having presence on these significant market place players will provide immediate access to huge customer base of these marketplace players and can leverage the massive advertising blitz used by these players for PrimDay Sale, billion-dollar day, Black Friday etc.,
<br/><br/>
Order Management system doesn’t have OOB integration with any of these market place players which is a big impediment in capturing and on-boarding new OMS customers in India Market.
The proposed asset tries to address this gap by providing an easier way to allow OMS customers to sell and capture order from market place channels.
Integrating and managing the integrations with each of the market place players distinctly costs huge time and effort for product team. Instead the asset takes the approach of integrating with Market Place Aggregator.
<br/><br/>
MarketPlace Aggregators integrate with different market place players and provide unified view of orders, inventory, catalog, price for end customer across all the channels. Some of the leading market place aggregators in India are SellerWorx, BrownTape, UniCommerce.
By integrating with marketplace aggregators, customers will get access to all big players (like Amazon, FlipKart, Snapdeal) at one go, without the need to understand the business model and API documentation of each player. Additionally, customers are spared from spending additional time and effort to maintain these assets, since the onus will be now on the aggregator to keep up to date with market place APIs.
<br/><br/>
To begin with, the asset will integrate with one such major aggregator in India – BrownTape. Asset provides features like fetching market place orders from Browntape and creating them in OMS system, updating the order status in Browntape at every stage of order fulfilment process, publishing the inventory to Browntape.
<br/><br/>
This asset is developed as an open source project and the code is hosted on IBM GitHub repository. Business partners and developers are free to download this asset and further customize it as per their needs. As part of asset, integration source code, ReadMe document, Installation Guide will be provided.
<br/>[Top](#table-of-contents)<br/><br/><br/>


## Getting started

#### Prerequisites

- Developer toolkit environment for Order Management on Cloud v17.3


#### Downloading the asset

Use git commands from your local developer toolkit system to download this asset. Alternately, download the repository zip, extract the contents, and get started.


#### Working with existing customizations

The files in this asset are structured as per customization directory structure suggested in developer toolkit. 

- Copy the files to your existing customization project directory. If you do not have one, create. 
- Modify the files as per your needs. (details in sections below)
- Follow the devtoolkit [importfromproject](https://www.ibm.com/support/knowledgecenter/SSGTJF/com.ibm.help.omcloud.custom.doc/customization/t_omc_customize_progenv_impext.html) step to deploy the customizations to your devtoolkit environment.
- Rebuild and deploy EAR and test your customization changes for integration with Browntape.
- Follow the devtoolkit [export](https://www.ibm.com/support/knowledgecenter/SSGTJF/com.ibm.help.omcloud.custom.doc/customization/t_omc_customize_progenv_expext.html) step to export customizations package to deploy on you IBM Order Management on Cloud environment.

<br/>[Top](#table-of-contents)<br/><br/><br/>

## Setting up Browntape

Please work with your Browntape account contact to setup admin access and API User access credentials.
- Use API user access credentials in the custom properties listed in the next section.

Work with your Browntape account contact to setup catalog, pricing information on market place and upload any existing orders.
<br/>[Top](#table-of-contents)<br/><br/><br/>

## Setting up IBM Order Management

#### Configuring custom properties

Add the below properties to your customer_overrides.properties file.

##### Mandatory properties
- `yfs.integration.marketplace.browntape.username` - Username to connect to Browntape
- `yfs.integration.marketplace.browntape.authstring` - Authstring to connect to Browntape
- `yfs.integration.marketplace.class` - Implementation of marketplace connector. Default - `com.ibm.sterling.integration.marketplace.provider.browntape.BTMarketPlaceConnector`
- `yfs.integration.marketplace.browntape.endpoint.order.get` - URL for Order fetch. Default - `http://app.browntape.com/0.1/orders/index.json`
- `yfs.integration.marketplace.browntape.endpoint.order.update` - URL for Order update. Default - `http://app.browntape.com/0.1/orders/updateorder.json`
- `yfs.integration.marketplace.browntape.endpoint.inv.get` - URL for Inventory fetch. Default - `http://app.browntape.com/0.1/skus/index.json`
- `yfs.integration.marketplace.browntape.endpoint.inv.update` - URL for Inventory update. Default - `http://app.browntape.com/0.1/skus/edit.json`

##### Optional properties
- `yfs.integration.marketplace.browntape.invallocpercent.default` - Global inventory allocation percentage for marketplace for all items, at all levels. For e.g. setting this to 50 means whatever onhand inventory is available for any item, only 50% of that inventory is available across all marketplaces, irrespective of any onhand invemtory level.
- `yfs.integration.marketplace.browntape.invallocpercent.default.forlevel.<level>` - Inventory allocation percentage for marketplace for all items, at specified level. This is the same as above, defined for each of the levels 0, 1, 2 and 3.
- `yfs.integration.marketplace.browntape.invallocpercent.<group>` - Inventory allocation percentage, for specified custom grouping, for all levels. For e.g. setting this to 50 means whatever onhand inventory is available for any item using this group, only 50% of that inventory is available across all marketplaces, irrespective of any onhand invemtory level.
- `yfs.integration.marketplace.browntape.invallocpercent.<group>.forlevel.<level>` - Inventory allocation percentage, for specified custom grouping, for specified level. This is the same as above, defined for each of the levels 0, 1, 2 and 3.
- `yfs.integration.marketplace.browntape.invallocpercent.grouping.foritem.<item>` - Custom inventory allocation grouping defined above used for specified item. If any item needs to define separate percentage allocations other than the default group, this value is set to the custom grouping defined above which defines those allocations.

Refer to `ATP Monitor Rule` configuration in below section [Configuring inventory and sourcing](#configuring-inventory-and-sourcing) for quantity boundaries.

- Level 0 - Above high quantity boundary
- Level 1 - Between high and medium quantity boundary
- Level 2 - Between medium and low quantity boundary
- Level 3 - Below low quantity boundary

<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring channels

To add different marketplaces as channels in OM, do the following steps-

- Note the channel id of the specific channel on Browntape website, say 21025 is the channel id for Amazon
- Prepare input from the following sample input (`CodeType` should be YCD_CHANNEL, `CodeValue` should prefix the channel id with BT-):

>&lt;CommonCode CodeLongDescription="Browntape - Amazon" CodeName="Browntape - Amazon" CodeShortDescription="Browntape - Amazon" CodeType="YCD_CHANNEL" CodeValue="BT-21025" MeantForEnterprise="Y" MeantForInternal="Y" OrganizationCode="DEFAULT" /&gt;

- Call `manageCommonCode` API using HTTP API Tester
- Repeat the same for other subscribed marketplaces.

You can now search orders in IBM Call Center for Commerce using the channels added above.<br/><br/>
![WCC_Order_Search](images/WCC_Order_Search.png?raw=true "WCC_Order_Search")
<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring participant model

As part of configing OMoC, you would have already configured your enterprise and node organizations. The below configurations are done based on a dummy enterprise MATRIX and 2 of its child nodes - M-WEST and M-SOUTH.
<br/><br/><br/>

#### Configuring inventory and sourcing

Create a Distribution group (`AllNodesDG`) under `Product Sourcing Distribution Groups`. Configure the node priorities.<br/><br/>
![Distribution_Group](images/Distribution_Group.png?raw=true "Distribution_Group")
<br/><br/><br/>

In the `ATP Rules` tab,  create an ATP Rule (`Matrix`). Configure the various time windows.<br/><br/>
![ATP_Rule](images/ATP_Rule.png?raw=true "ATP_Rule")
<br/><br/><br/>

In `Monitor Rules tab`, check `Use Activity-Based Mode For RTAM`. Subscribe to `AllNodesDG` as the DG to use for Group level monitoring.<br/><br/>
![Monitor_Rule2](images/Monitor_Rule2.png?raw=true "Monitor_Rule2")
<br/><br/><br/>

In the same tab, create an ATP Monitor Rule (`Matrix`) and configure the lead time override and quantity boundaries for different levels.<br/><br/>
![Monitor_Rule](images/Monitor_Rule.png?raw=true "Monitor_Rule")
<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring items

Whichever items are available for ordering on Browntape marketplaces must be manually created on OM too. Below is just for reference. <br/><br/>
![Items](images/Items.png?raw=true "Items")
<br/><br/><br/>

Publish the items. Under `Inventory Info` tab in item details for each such item, select the ATP Rule (`Matrix`) and ATP Monitor Rule (`Matrix`) created above.<br/><br/>
![Items2](images/Items2.png?raw=true "Items2")
<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring RTAM agent

Open RTAM agent transaction. Save the OOB criteria `REALTIME_ATP_MONITOR_OP1` as a custom criteria and configure it.<br/><br/>
![RTAM](images/RTAM.png?raw=true "RTAM")
<br/><br/><br/>

Create a synchronous service `BTUpdateAvailability` as shown in the images below. <br/><br/>
![BTUpdateAvailability_Service](images/BTUpdateAvailability_Service.png?raw=true "BTUpdateAvailability_Service")
<br/><br/><br/>

Set the API and Method name as `updateAvailability`, Class name as `com.ibm.sterling.integration.marketplace.provider.browntape.BTUpdateManager` <br/><br/>
![BTUpdateAvailability_Service2](images/BTUpdateAvailability_Service2.png?raw=true "BTUpdateAvailability_Service2")
<br/><br/><br/>

Create an action `BTUpdateAvailability` and associate the above service to it as an invoked service. <br/><br/>
![BTUpdateAvailability_Action](images/BTUpdateAvailability_Action.png?raw=true "BTUpdateAvailability_Action")
<br/><br/><br/>

Open RTAM agent transaction again. Activate the event `On Real Time Availability Change List` and add the above action in the event handler definition. <br/><br/>
![RTAM2](images/RTAM2.png?raw=true "RTAM2")
<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring order sync agent

The order sync agent syncs freshly created orders on Browntape (with status `Processing`) to OM. Once they are created on OM, the same orders are updated to the status `Order Accepted` on Browntape.

- Go to `Order Fulfillment` under `Process Modelling`. 
- Create a new time-triggered transaction `BT Order Sync` with class name - `com.ibm.sterling.integration.marketplace.provider.browntape.BTOrderSyncIntegration`. 
- Create an agent criteria `BT_ORDER_SYNC` and configure it. 
- Keep the trigger interval low ~ 1 minute.
- In the Criteria Parameters window, add parameter EnterpriseCode=&lt;Your enterprise code e.g. MATRIX&gt;<br/><br/>

![BT_Order_Sync](images/BT_Order_Sync.png?raw=true "BT_Order_Sync")
<br/>[Top](#table-of-contents)<br/><br/><br/>

#### Configuring change order

To propagate order status changes from OM to Browntape, the OOB `Change Order` transaction is extended. OOB, the following status changes are propagated to Browntape-

- Status on OM `Created` => Status on Browntape `Order Accepted`
- Status on OM `Released` or `Awating Shipment Consolidation` => Status on Browntape `Ready to ship`

Create a synchronous service `BTUpdateOrder` as shown in the images below.<br/><br/>
![BTUpdateOrder_Service](images/BTUpdateOrder_Service.png?raw=true "BTUpdateOrder_Service")
<br/><br/><br/>

Set the API and Method name as `updateOrder`, Class name as `com.ibm.sterling.integration.marketplace.provider.browntape.BTUpdateManager`<br/><br/>
![BTUpdateOrder_Service2](images/BTUpdateOrder_Service2.png?raw=true "BTUpdateOrder_Service2")
<br/><br/><br/>

Create an action `BTUpdateOrder` and associate the above service to it as an invoked service. <br/><br/>
![BTUpdateOrder_Action](images/BTUpdateOrder_Action.png?raw=true "BTUpdateOrder_Action")
<br/><br/><br/>

Open `Change Order` transaction. Activate the event `On Order Release Status Change` and add the above action in the event handler definition. <br/><br/>
![Order_Change](images/Order_Change.png?raw=true "Order_Change")
<br/>[Top](#table-of-contents)<br/><br/><br/>


#### Extending custom implementation 

Though OOB implementations are provided to easily get started on Browntape integration, these implementations can be extended to suit specific needs of the implementation not handled by the OOB implementations.

- Marketplace connector- A custom connector implementation can be defined (and added to the property `yfs.integration.marketplace.class`) by extending the interface `com.ibm.sterling.integration.marketplace.core.MarketPlaceConnector`
- Order Sync agent - The order sync agent class can be extended in case more order attributes need to be stored while creating orders on OM, or when order attribute mappings change. This custom class then needs to be set in the Order Sync agent transaction configuration.
- UpdateManager - The UpdateManager implementation uses the Marketplace connector to interact with Browntape, but can be extended if required, for e.g. for more status updates to Browntape than provided OOB. This custom class then needs to be set in the update services configured above.

<br/>[Top](#table-of-contents)<br/><br/><br/>


## Disclaimer

The asset(s) provided on or through this asset repository is provided “as is” and “as available” basis. Use of the asset(s) and services offered in this asset repository is at the sole discretion of its users. 

IBM makes no statement, representation, or warranty about the accuracy or completeness of any content contained in this asset repository. IBM disclaims all responsibility and all liability (including without limitation, liability in negligence) for all expenses, losses, damages and costs you might incur as a result of the content being inaccurate or incomplete in any way for any reason.

IBM disclaims all liability for any damages arising from your access to, use of, or downloading of any material or part thereof from this asset repository.

<br/>[Top](#table-of-contents)<br/><br/><br/>
