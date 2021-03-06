/*global define*/
define([
    "common-objects/views/attributes/attribute_list_item",
    "text!common-objects/templates/attributes/attribute_list_item.html",
    "text!common-objects/templates/attributes/attribute_list_item_text.html"
], function (AttributeListItemView, attribute_list_item, template) {
    var AttributeListItemTextView = AttributeListItemView.extend({

        template: template,
        partials: {
            attribute_list_item: attribute_list_item
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        }
    });
    return AttributeListItemTextView;
});
