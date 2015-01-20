/*global define,App*/
define(
    [
        "backbone",
        "mustache",
        "text!templates/control_options.html"
    ],
    function (Backbone, Mustache, template) {

        var ControlOptionsView = Backbone.View.extend({

            className: "side_control_group",

            events: {
                "click button#gridSwitch": "gridSwitch",
                "click button#screenshot": "takeScreenShot",
                "click button#show_edited_meshes": "show_edited_meshes",
                "click button#oculus": "toggleOculus"
            },

            gridSwitch: function () {
                var gridSwitch = this.$("#gridSwitch");
                gridSwitch.toggleClass("active");
                App.SceneOptions.grid = !!gridSwitch.hasClass("active");
            },

            takeScreenShot: function () {
                App.sceneManager.takeScreenShot();
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                return this;
            },

            show_edited_meshes: function () {
                this.$('#show_edited_meshes').toggleClass("active");
                if (this.$('#show_edited_meshes').hasClass("active")) {
                    App.sceneManager.colourEditedMeshes();
                } else {
                    App.sceneManager.cancelColourEditedMeshes();
                }
            },

            toggleOculus:function(){
                this.$('#oculus').toggleClass("active");
                App.sceneManager.toggleOculus(this.$('#oculus').hasClass("active"));
            }


        });

        return ControlOptionsView;

    });
