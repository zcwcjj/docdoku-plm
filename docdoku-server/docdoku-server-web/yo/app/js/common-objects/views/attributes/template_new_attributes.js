/*global _,define*/
define([
    'backbone',
    'common-objects/views/base',
    'common-objects/views/attributes/template_new_attribute_list',
    'text!common-objects/templates/attributes/template_new_attributes.html'
], function (Backbone,BaseView, TemplateNewAttributeListView, template) {
	'use strict';
    var TemplateNewAttributesView = BaseView.extend({

        template: template,

        attributesLocked: false,

        collection: function () {
            return new Backbone.Collection();
        },

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.events['click .add'] = this.addAttribute;
            this.events['change .lock input'] = this.attributesLockedChange;
            this.events['update-sort'] = this.updateSort;

            if (this.options.attributesLocked) {
                this.attributesLocked = this.options.attributesLocked;
            }
        },

        updateSort: function(event,model,index){
            this.collection.remove(model,{silent:true});
            this.collection.add(model, {at: index,silent:true});
        },

        rendered: function () {
            this.attributesView = this.addSubView(
                new TemplateNewAttributeListView({
                    el: '#items-' + this.cid,
                    collection: this.collection
                })
            );

            this.$el.find('.lock input')[0].checked = this.attributesLocked;
            this.$el.toggleClass('attributes-locked', this.attributesLocked);

            this.attributesView.$el.sortable({
                handle: ".sortable-handler",
                placeholder: "list-item well highlight",
                stop: function(event, ui) {
                    ui.item.trigger('drop', ui.item.index());
                }
            });
        },

        addAttribute: function () {
            this.collection.add({
                name: '',
                attributeType: 'TEXT'
            });
        },

        attributesLockedChange: function (e) {
            this.attributesLocked = e.target.checked;

            if (!this.attributesLocked) {
                _.map(this.collection.models, function (attribute) {
                    return attribute.set('mandatory', false);
                });
            }

            this.$el.toggleClass('attributes-locked', this.attributesLocked);
        },

        isAttributesLocked: function () {
            return this.attributesLocked;
        }
    });
    return TemplateNewAttributesView;
});
