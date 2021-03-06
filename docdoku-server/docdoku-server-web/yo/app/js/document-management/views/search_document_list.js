/*global define,App*/
define([
    'collections/search_document',
    'views/content_document_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!templates/search_document_form.html',
    'text!templates/search_document_list.html'
], function (SearchDocumentList, ContentDocumentListView, deleteButton, checkoutButtonGroup, tagsButton, newVersionButton, aclButton, searchForm, template) {
	'use strict';
	var SearchDocumentListView = ContentDocumentListView.extend({
        template: template,

        partials: {
            deleteButton: deleteButton,
            checkoutButtonGroup: checkoutButtonGroup,
            tagsButton: tagsButton,
            newVersionButton: newVersionButton,
            searchForm: searchForm,
            aclButton: aclButton
        },

        collection: function () {
            return new SearchDocumentList().setQuery(this.options.query);
        },

        initialize: function () {

            ContentDocumentListView.prototype.initialize.apply(this, arguments);

            if (this.model) {
                this.collection.parent = this.model;
            }

            this.templateExtraData = {
                isReadOnly: App.appView.isReadOnly()
            };
        }
    });
    return SearchDocumentListView;
});
