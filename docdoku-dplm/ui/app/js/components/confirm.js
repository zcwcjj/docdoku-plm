(function(){

    'use strict';

    angular.module('dplm.services.confirm',[])

        .service('ConfirmService',function($q,$mdDialog){

            this.confirm = function($event,confirmOptions){
                var deferred = $q.defer();
                var confirmed = false;
                $mdDialog.show({
                    //targetEvent: $event,
                    template:
                    '<md-dialog>' +
                    '  <md-content>'+confirmOptions.content+'</md-content>' +
                    '  <div class="md-actions">' +
                    '    <md-button aria-label="" class="md-default md-raised"  ng-click="cancel()">' +
                    '      {{\'NO\' | translate }}' +
                    '    </md-button>' +
                    '    <md-button aria-label="" class="md-primary md-raised"  ng-click="confirm()">' +
                    '      {{\'YES\' | translate }}' +
                    '    </md-button>' +
                    '  </div>' +
                    '</md-dialog>',
                    controller: function($scope){
                        $scope.cancel = function(){
                            $mdDialog.hide();
                        };
                        $scope.confirm = function(){
                            confirmed = true;
                            $mdDialog.hide();
                        };
                    },
                    onComplete: afterShowAnimation
                }).finally(function() {
                    if(confirmed){
                        deferred.resolve();
                    }else{
                        deferred.reject();
                    }
                });
                // When the 'enter' animation finishes...
                function afterShowAnimation(scope, element, options) {
                }

                return deferred.promise;
            };

        });

})();
