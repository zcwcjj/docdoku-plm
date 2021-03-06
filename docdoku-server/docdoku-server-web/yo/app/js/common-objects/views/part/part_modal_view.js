/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/components/modal',
    'common-objects/views/file/file_list',
    'text!common-objects/templates/part/part_modal.html',
    'common-objects/views/attributes/attributes',
    'common-objects/views/part/parts_management_view',
    'common-objects/views/part/modification_notification_list_view',
    'common-objects/views/linked/linked_documents',
    'common-objects/views/alert',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/workflow/lifecycle',
    'common-objects/views/part/conversion_status_view',
    'common-objects/utils/date',
    'common-objects/views/tags/tag',
    'common-objects/models/tag'
], function (Backbone, Mustache, ModalView, FileListView, template, AttributesView, PartsManagementView, ModificationNotificationListView, LinkedDocumentsView, AlertView, LinkedDocumentCollection, LifecycleView, ConversionStatusView, date,TagView,Tag) {
    'use strict';
    var PartModalView = ModalView.extend({

        initialize: function () {
            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();

            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click a#previous-iteration'] = 'onPreviousIteration';
            this.events['click a#next-iteration'] = 'onNextIteration';
            this.events['submit #form-part'] = 'onSubmitForm';
            this.events['click .action-checkin'] = 'actionCheckin';
            this.events['click .action-checkout'] = 'actionCheckout';
            this.events['click .action-undocheckout'] = 'actionUndoCheckout';
            this.tagsToRemove = [];
        },

        onPreviousIteration: function () {
            if (this.iterations.hasPreviousIteration(this.iteration)) {
                this.switchIteration(this.iterations.previous(this.iteration));
            }
            return false;
        },

        onNextIteration: function () {
            if (this.iterations.hasNextIteration(this.iteration)) {
                this.switchIteration(this.iterations.next(this.iteration));
            }
            return false;
        },

        switchIteration: function (iteration) {
            this.iteration = iteration;
            var activeTabIndex = this.getActiveTabIndex();
            this.render();
            this.activateTab(activeTabIndex);
        },

        getActiveTabIndex: function () {
            return this.$tabs.filter('.active').index();
        },

        activateTab: function (index) {
            this.$tabs.eq(index).children().tab('show');
        },

        activateFileTab: function(){
            this.activateTab(3);
        },

        render: function () {

            var data = {
                part: this.model,
                i18n: App.config.i18n,
                permalink: this.model.getPermalink()
            };

            this.editMode = this.model.isCheckoutByConnectedUser() && this.iterations.isLast(this.iteration);
            data.editMode = this.editMode;
            data.isLockedMode = !this.iteration || (this.model.isCheckout() && this.model.isLastIteration(this.iteration.getIteration()) && !this.model.isCheckoutByConnectedUser());
            data.isCheckout = this.model.isCheckout() ;
            this.isCheckout = data.isCheckout ;
            data.isReleased = this.model.attributes.status == "RELEASED" ;
            this.isReleased = this.model.attributes.status == "RELEASED" ;
            data.isShowingLast = this.iterations.isLast(this.iteration);

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iterations = this.model.getIterations().length;
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.reference = this.iteration.getReference();
                data.iteration.creationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.creationDate
                );
                data.iteration.modificationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.modificationDate
                );
                data.iteration.revisionDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.revisionDate
                );
            }

            this.$el.html(Mustache.render(template, data));

            this.$authorLink = this.$('.author-popover');
            this.$checkoutUserLink = this.$('.checkout-user-popover');

            this.$inputIterationNote = this.$('#inputRevisionNote');
            this.$tabs = this.$('.nav-tabs li');

            this.bindUserPopover();
            if (this.iteration) {
                this.initCadFileUploadView();
                this.initAttributesView();

                this.initPartsManagementView();
                this.initModificationNotificationListView();

                this.initLinkedDocumentsView();
                this.initLifeCycleView();
            }

            date.dateHelper(this.$('.date-popover'));
            this.tagsManagement(this.editMode);
            return this;
        },

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getAuthorLogin(), this.model.getNumber(), 'right');
            if (this.model.isCheckout()) {
                this.$checkoutUserLink.userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'right');
            }
        },

        initAttributesView: function () {

            var that = this;

            this.attributes = new Backbone.Collection();

            this.attributesView = new AttributesView({
                el: this.$('#attributes-list')
            });

            this.attributesView.setAttributesLocked(this.model.isAttributesLocked());

            this.attributesView.setEditMode(this.editMode);
            this.attributesView.render();

            _.each(this.iteration.getAttributes().models, function (item) {
                that.attributesView.addAndFillAttribute(item);
            });

        },

        onSubmitForm: function (e) {

            // cannot pass a collection of cad file to server.
            var cadFile = this.fileListView.collection.first();
            if (cadFile) {
                this.iteration.set('nativeCADFile', cadFile.get('fullName'));
            } else {
                this.iteration.set('nativeCADFile', '');
            }
            var that = this;
            this.iteration.save({
                iterationNote: this.$inputIterationNote.val(),
                components: this.partsManagementView.collection.toJSON(),
                instanceAttributes: this.attributesView.collection.toJSON(),
                linkedDocuments: this.linkedDocumentsView.collection.toJSON()
            }, {
                success: function () {
                    if (that.model.collection){
                        that.model.collection.fetch();
                    }
                    that.model.fetch();
                    that.hide();
                    that.model.trigger('change');
                },
                error: this.onError
            });


            this.fileListView.deleteFilesToDelete();
            that.deleteClickedTags();


            e.preventDefault();
            e.stopPropagation();

            return false;
        },

        initCadFileUploadView: function () {

            this.fileListView = new FileListView({
                baseName: this.iteration.getBaseName(),
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                collection: this.iteration._nativeCADFile,
                editMode: this.editMode,
                singleFile: true
            }).render();

            this.$('#iteration-files').html(this.fileListView.el);

            if(this.editMode){
                this.conversionStatusView = new ConversionStatusView({model:this.iteration}).render();
                this.$('#iteration-files').append(this.conversionStatusView.el);
            }

        },

        initPartsManagementView: function () {
            this.partsManagementView = new PartsManagementView({
                el: '#iteration-components',
                collection: new Backbone.Collection(this.iteration.getComponents()),
                editMode: this.editMode,
                isReleased:this.isReleased,
                isCheckout: this.isCheckout,
                model: this.model
            }).render();
        },

        initModificationNotificationListView: function () {
            this.modificationNotificationListView = new ModificationNotificationListView({
                el: '#iteration-modification-notifications',
                model: this.model
            }).render();
        },

        initLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                commentEditable:true,
                documentIteration: this.iteration,
                collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        initLifeCycleView: function () {
            var that = this;
            if (this.model.get('workflow')) {

                this.lifecycleView = new LifecycleView({
                    el: '#tab-iteration-lifecycle'
                }).setAbortedWorkflowsUrl(this.model.getUrl() + '/aborted-workflows').setWorkflow(this.model.get('workflow')).setEntityType('parts').render();

                this.lifecycleView.on('lifecycle:change', function () {
                    that.model.fetch({success: function () {
                        that.lifecycleView.setAbortedWorkflowsUrl(that.model.getUrl() + '/aborted-workflows').setWorkflow(that.model.get('workflow')).setEntityType('parts').render();
                    }});
                });

            } else {
                this.$('a[href=#tab-iteration-lifecycle]').hide();
            }
        },
        tagsManagement: function (editMode) {

            var $tagsZone = this.$('.master-tags-list');
            var that = this;

            _.each(this.model.attributes.tags, function (tagLabel) {

                var tagView;

                var tagViewParams = editMode ?
                {
                    model: new Tag({id: tagLabel, label: tagLabel}),
                    isAdded: true,
                    clicked: function () {
                        that.tagsToRemove.push(tagLabel);
                        tagView.$el.remove();
                    }} :
                {
                    model: new Tag({id: tagLabel, label: tagLabel}),
                    isAdded: true,
                    clicked: function () {
                        that.tagsToRemove.push(tagLabel);
                        tagView.$el.remove();
                        that.model.removeTag(tagLabel, function () {
                            if (that.model.collection.parent) {
                                if (_.contains(that.tagsToRemove, that.model.collection.parent.id)) {
                                    that.model.collection.remove(that.model);
                                }
                            }
                        });
                        tagView.$el.remove();
                    }
                };

                tagView = new TagView(tagViewParams).render();

                $tagsZone.append(tagView.el);

            });

        },

        deleteClickedTags: function () {
            if (this.tagsToRemove.length) {
                var that = this;
                this.model.removeTags(this.tagsToRemove, function () {
                    if (that.model.collection.parent) {
                        if (_.contains(that.tagsToRemove, that.model.collection.parent.id)) {
                            that.model.collection.remove(that.model);
                        }
                    }
                });
            }
        },
        actionCheckin: function () {
            if (!this.model.getLastIteration().get('iterationNote')) {

                var note = this.setRevisionNote();
                this.iteration.save({
                    iterationNote: note

                }).success(function () {
                    this.model.checkin().success(function () {
                        this.onSuccess();
                    }.bind(this));
                }.bind(this));

            } else {
                this.model.checkin().success(function () {
                    this.onSuccess();
                }.bind(this));
            }
        }
        ,

        actionCheckout: function () {
            var self = this;
            self.model.checkout().success(function () {
                self.onSuccess();
            });

        },
        actionUndoCheckout: function () {
            var self = this;
            self.model.undocheckout().success(function () {
                self.onSuccess();
            });

        },
        setRevisionNote: function () {
            var note;
            if (_.isEqual(this.$('#inputRevisionNote').val(), '')) {
                note = null;

            } else {
                note = this.$('#inputRevisionNote').val();
            }
            return note;
        },

        onSuccess: function () {
            this.model.fetch().success(function () {
                this.iteration = this.model.getLastIteration();
                this.iterations = this.model.getIterations();
                this.render();
                this.activateTab(1);

            }.bind(this));

        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$el.find('.notifications').first().append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        }

    });

    return PartModalView;

});
