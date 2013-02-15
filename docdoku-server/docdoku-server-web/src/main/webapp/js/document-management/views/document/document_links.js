define([
    "collections/document_iteration",
    "views/document/document_link",
    "text!templates/document/document_links.html",
    "i18n!localization/nls/document-management-strings"
], function(DocumentIterationCollection, DocumentLinkView, template, i18n) {
    var DocumentLinksView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-documents-view',

        initialize: function() {
            this.searchResults = [];
        },

        render: function() {
            var self = this;

            this.$el.html(Mustache.render(template,
                {
                    i18n: i18n,
                    editMode: this.options.editMode,
                    view: this
                }
            ));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function(linkedDocument) {
                self.addLinkView(linkedDocument);
            });

            return this;
        },

        bindDomElements: function() {
            this.documentReferenceInput = this.$("#document-reference-typeahead");
            this.linksUL = this.$("#linked-docs-" + this.cid);
        },

        addLinkView: function(linkedDocument) {
            var self = this;
            var linkView = new DocumentLinkView({
                editMode: this.options.editMode,
                model: linkedDocument
            }).render();

            this.linksUL.append(linkView.el);
        },

        bindTypeahead: function() {
            var self = this;

            this.documentReferenceInput.typeahead({
                source: function(query, process) {
                    $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/documents/docs_last_iter?q=' + query, function(data) {

                        self.searchResults = new DocumentIterationCollection(data);

                        // Remove documents that are already linked
                        var docsToRemove = [];
                        self.searchResults.each(
                            function(documentIteration) {
                                var linkedDoc = self.collection.find(
                                    function(linkedDocument) {
                                        return linkedDocument.getDocKey() == documentIteration.getDocKey();
                                    });
                                if(!_.isUndefined(linkedDoc))
                                    docsToRemove.push(documentIteration);
                            }
                        );
                        self.searchResults.remove(docsToRemove);

                        process(self.searchResults.map(function(docLastIter) {
                            return docLastIter.getDocKey();
                        }));
                    })
                },

                sorter: function(docsLastIterDocKey) {
                    return docsLastIterDocKey.sort();
                },

                updater: function(docLastIterDocKey) {
                    var linkedDocument = self.searchResults.find(function(docLastIter) {
                        return docLastIter.getDocKey() == docLastIterDocKey;
                    });
                    self.collection.add(linkedDocument);

                    self.addLinkView(linkedDocument);
                }
            });
        }

    });
    return DocumentLinksView;
});