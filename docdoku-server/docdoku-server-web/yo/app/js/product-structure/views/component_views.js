/*global _,define,App*/
define([
    'backbone',
    'common-objects/models/part',
    'common-objects/views/part/part_modal_view'
], function (Backbone, Part, PartModalView) {
	'use strict';
    var expandedViews = [];
    var ComponentViews = {};

    ComponentViews.Components = Backbone.View.extend({
        tagName: 'ul',

        initialize: function () {
            this.options.parentView.append(this.el);
            this.componentViews = [];
            this.expandedViews = [];
            if (this.collection.isEmpty()) {
                this.listenTo(this.collection, 'reset', this.addAllComponentsView)
                    .listenTo(this.collection, 'add', this.addComponentView);
                this.collection.fetch({reset: true});
            } else {
                this.addAllComponentsView();
            }
        },

        addAllComponentsView: function () {
            this.collection.each(this.addComponentView, this);
        },

        addComponentView: function (component) {

            var isLast = component === this.collection.last();

            var optionsForComponentView = {
                model: component,
                isLast: isLast,
                checkedAtInit: this.options.parentChecked || App.partsTreeView.smartPath.indexOf(component.getPath()) !== -1,
                resultPathCollection: this.options.resultPathCollection
            };

            var componentView = component.isAssembly() ? new ComponentViews.Assembly(optionsForComponentView) : new ComponentViews.Leaf(optionsForComponentView);

            this.$el.append(componentView.render().el);

            // expand the assembly if it was expanded before redraw && _.contains(expandedViews,component.attributes.number)
            if (component.isAssembly() && _.contains(expandedViews, component.attributes.number)) {
                componentView.onToggleExpand();
            }

            this.componentViews.push(componentView);
        },

        fetchAll: function () {
            this.$el.empty();
            this.collection.fetch({reset: true});
        }

    });

    ComponentViews.Leaf = Backbone.View.extend({

        tagName: 'li',

        template: _.template('<%if(!isLock && !isForbidden) {%>' +
            '<input type="checkbox" class="available" <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
            '<%} else {%>' +
            '<input type="checkbox" disabled <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
            '<%}%>' +
            '<a><label class="checkbox"><%= number %> (<%= amount %> <%= unit %>)</label></a>' +
            '<%if(isForbidden) {%> ' +
            '<i class="fa fa-file"></i>' +
            '<i class="fa fa-ban"></i>' +
            '<%} else if(isCheckoutByAnotherUser) {%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-lock"></i>' +
            '<%} else if(isCheckoutByConnectedUser) {%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-pencil"></i>' +
            '<%} else if(isReleased){%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-check"></i>' +
            '<%} else{%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-eye"></i>' +
            '<%}%>'
        ),

        events: {
            'click a': 'onComponentSelected',
            'change input:first': 'onChangeCheckbox',
            'click .openModal:first': 'onEditPart'
        },

        initialize: function () {
            _.bindAll(this, ['onChangeCheckbox']);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
            this.$el.attr('id', 'path_' + String(this.model.attributes.path));
            this.isForbidden = this.model.isForbidden();
            this.isLock = this.model.isCheckout() && this.model.isLastIteration(this.model.get('iteration')) && !this.model.isCheckoutByConnectedUser();
        },

        onAllResultPathAdded: function () {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass('resultPath');
            } else {
                this.$el.removeClass('resultPath');
            }
        },

        onChangeCheckbox: function (event) {
            if (event.target.checked) {
                App.instancesManager.loadComponent(this.model);
            }
            else {
                App.instancesManager.unLoadComponent(this.model);
            }
        },

        render: function () {
            var data = {
                number: this.model.attributes.number,
                amount: this.model.getAmount(),
                unit: this.model.getUnit(),
                checkedAtInit: this.options.checkedAtInit,
                isForbidden: this.model.isForbidden(),
                isCheckoutByAnotherUser: this.model.isCheckout() && !this.model.isCheckoutByConnectedUser(),
                isCheckoutByConnectedUser: this.model.isCheckout() && this.model.isCheckoutByConnectedUser(),
                isReleased: this.model.isReleased(),
                isLock: this.isLock
            };

            this.$el.html(this.template(data));

            this.input = this.$('>input');

	        //If the ComponentViews is checked
	        if(this.options.checkedAtInit){
		        App.instancesManager.loadComponent(this.model);
	        }

            if (this.options.isLast) {
                this.$el.addClass('last');
            }

            this.onAllResultPathAdded();

            return this;
        },

        onComponentSelected: function (e) {
            e.stopPropagation();
            this.$('>a').trigger('component:selected', [this.model, this.$el]);
        },

	    onEditPart: function () {
		    var model = new Part({partKey: this.model.getNumber() + '-' + this.model.getVersion()});
		    model.fetch().success(function () {
			    new PartModalView({
				    model: model
			    }).show();
		    });

	    }
    });

    ComponentViews.Assembly = Backbone.View.extend({

        tagName: 'li',

        className: 'expandable',

        template: _.template('<%if(!isLock && !isForbidden) {%>' +
            '<div class="hitarea expandable-hitarea"></div>' +
            '<input type="checkbox" class="available" <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
            '<%} else {%>' +
            '<input type="checkbox" disabled <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
            '<%}%>' +
            '<a><label class="checkbox isNode"><%= number %> (<%= amount %>)</label></a>' +
            '<%if(isForbidden) {%> ' +
            '<i class="fa fa-file"></i>' +
            '<i class="fa fa-ban"></i>' +
            '<%} else if(isCheckoutByAnotherUser) {%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-lock"></i>' +
            '<%} else if(isCheckoutByConnectedUser) {%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-pencil"></i> ' +
            '<%} else if(isReleased){%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-check"></i>' +
            '<%} else{%> ' +
            '<i class="fa fa-file openModal"></i>' +
            '<i class="fa fa-eye"></i>' +
            '<%}%>'
        ),

        events: {
            'click a:first': 'onComponentSelected',
            'click .openModal:first': 'onEditPart',
            'change input:first': 'onChangeCheckbox',
            'click .hitarea:first': 'onToggleExpand'
        },

        initialize: function () {
            this.isExpanded = false;
            _.bindAll(this, ['onChangeCheckbox']);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
            this.$el.attr('id', 'path_' + String(this.model.attributes.path));
            this.isForbidden = this.model.isForbidden();
            this.isLock = this.model.isCheckout() && this.model.isLastIteration(this.model.get('iteration')) && !this.model.isCheckoutByConnectedUser();
        },

        onAllResultPathAdded: function () {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass('resultPath');
            } else {
                this.$el.removeClass('resultPath');
            }
        },

        onChangeCheckbox: function (event) {
            if (event) {
                if (event.target.checked) {
                    App.instancesManager.loadComponent(this.model);
                }
                else {
                    App.instancesManager.unLoadComponent(this.model);
                }
            }
        },

        render: function () {

            var data = {
                number: this.model.attributes.number,
                amount: this.model.getAmount(),
                unit: this.model.getUnit(),
                checkedAtInit: this.options.checkedAtInit,
                isForbidden: this.isForbidden,
                isCheckoutByAnotherUser: this.model.isCheckout() && !this.model.isCheckoutByConnectedUser(),
                isCheckoutByConnectedUser: this.model.isCheckout() && this.model.isCheckoutByConnectedUser(),
                isReleased: this.model.isReleased(),
                isLock: this.isLock
            };

            this.$el.html(this.template(data));

            this.input = this.$('>input');

	        //If the ComponentViews is checked
	        if(this.options.checkedAtInit && (!App.collaborativeView || !App.collaborativeView.roomKey)){
		        App.instancesManager.loadComponent(this.model);
	        }

            if (data.isForbidden || data.isLock) {
                this.$el.removeClass('expandable');
            }

            if (this.options.isLast) {
                if (data.isForbidden || data.isLock) {
                    this.$el.addClass('last');
                } else {
                    this.$el.addClass('lastExpandable')
                        .children('.hitarea')
                        .addClass('lastExpandable-hitarea');
                }
            }

            this.onAllResultPathAdded();


            if (_.contains(this.expandedViews, this.model.attributes.number)) {
                this.onToggleExpand();
            }

            return this;
        },

        onToggleExpand: function () {
            if (!this.hasChildrenNodes()) {
                new ComponentViews.Components({
                    collection: this.model.children,
                    parentView: this.$el,
                    parentChecked: this.isChecked(),
                    resultPathCollection: this.options.resultPathCollection
                });
            }
            this.toggleExpand();
        },

        toggleExpand: function () {
            if (!this.isForbidden && !this.isLock) {
                this.$el.toggleClass('expandable collapsable')
                    .children('.hitarea')
                    .toggleClass('expandable-hitarea collapsable-hitarea');

                if (this.options.isLast) {
                    this.$el.toggleClass('lastExpandable lastCollapsable')
                        .children('.hitarea')
                        .toggleClass('lastExpandable-hitarea lastCollapsable-hitarea');
                }

                this.isExpanded = !this.isExpanded;

                var childrenNode = this.$('>ul');

                if (this.isExpanded) {
                    childrenNode.show();
                    expandedViews.push(this.model.attributes.number);
                } else {
                    childrenNode.hide();
                    expandedViews = _(expandedViews).without(this.model.attributes.number);
                }
            }
        },

        hasChildrenNodes: function () {
            var childrenNode = this.$('>ul');
            return childrenNode.length > 0;
        },

        onComponentSelected: function (e) {
            e.stopPropagation();
            this.$('>a').trigger('component:selected', [this.model, this.$el]);
        },

        isChecked: function () {
            return this.input.prop('checked');
        },

	    onEditPart: function () {
		    var model = new Part({partKey: this.model.getNumber() + '-' + this.model.getVersion()});
		    model.fetch().success(function () {
			    new PartModalView({
				    model: model
			    }).show();
		    });

	    }
    });

    return ComponentViews;
});
