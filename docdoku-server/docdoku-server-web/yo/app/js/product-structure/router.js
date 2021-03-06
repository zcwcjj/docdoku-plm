/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({

        routes: {
            ':workspaceId/:productId': 'defaults',
            ':workspaceId/:productId/scene(/camera/:camera)(/target/:target)(/up/:up)': 'scene',
            ':workspaceId/:productId/bom': 'bom',
            ':workspaceId/:productId/room/:key': 'joinCollaborative'
        },

        defaults: function (workspaceId, productId) {
            this.navigate(workspaceId+'/'+productId+'/scene',{trigger:true});
        },

        scene:function(workspaceId, productId, camera, target, up){
            App.appView.sceneMode();
            if (camera && target && up) {
                var c = camera.split(';');
                var t = target.split(';');
                var u = up.split(';');
                App.sceneManager.setControlsContext({
                    target: {x:parseFloat(t[0]),y: parseFloat(t[1]),z: parseFloat(t[2])},
                    camPos: {x:parseFloat(c[0]),y: parseFloat(c[1]),z: parseFloat(c[2])},
                    camOrientation: {x:parseFloat(u[0]),y:parseFloat(u[1]),z: parseFloat(u[2])}
                });
            }
        },

        bom:function(workspaceId, productId){
            App.appView.bomMode();
        },

        joinCollaborative: function (workspaceId, productId, key) {
            App.appView.sceneMode();
            if (!App.collaborativeView.isMaster) {
                App.appView.requestJoinRoom(key);
            }
        },

        updateRoute: function (context) {

            if(!App.collaborativeView){

                var c = context.camPos.toArray();
                var t = context.target.toArray();
                var u = context.camOrientation.toArray();
                var positionPrecision = 2;

                this.navigate(
                    App.config.workspaceId+'/'+App.config.productId+'/scene' +
                    '/camera/' +
                    c[0].toFixed(positionPrecision) + ';' +
                    c[1].toFixed(positionPrecision) + ';' +
                    c[2].toFixed(positionPrecision) +
                    '/target/' +
                    t[0].toFixed(positionPrecision) + ';' +
                    t[1].toFixed(positionPrecision) + ';' +
                    t[2].toFixed(positionPrecision) +
                    '/up/' +
                    u[0].toFixed(positionPrecision) + ';' +
                    u[1].toFixed(positionPrecision) + ';' +
                    u[2].toFixed(positionPrecision),{
                    trigger: false
                });

            }
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
