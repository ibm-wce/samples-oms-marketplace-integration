scDefine(["dojo/text!./templates/OrderSearchExtn.html", "scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/_base/lang", "scbase/loader!dojo/text", "scbase/loader!idx/form/FilteringSelect", "scbase/loader!sc/plat", "scbase/loader!sc/plat/dojo/binding/ComboDataBinder", "scbase/loader!sc/plat/dojo/utils/BaseUtils"], function (templateText,
		_dojodeclare, _dojokernel, _dojolang, _dojotext, _idxFilteringSelect, _scplat, _scComboDataBinder, _scBaseUtils) {
	return _dojodeclare("extn.order.search.OrderSearchExtnUI", [], {
			templateString: templateText,
			namespaces: {
			targetBindingNamespaces:[],
			sourceBindingNamespaces:[{
					value: 'extn_getEntryTypeList',
					scExtensibilityArrayItemId: 'extn_SourceNamespaces_7'}]
		},
		hotKeys: [ ],
		events: [ ],
		subscribers: { 
			local: []
		}
	});
});