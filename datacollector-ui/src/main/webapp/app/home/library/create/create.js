/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Controller for Library Pane Create Modal.
 */

angular
  .module('dataCollectorApp.home')
  .controller('CreateModalInstanceController', function ($scope, $modalInstance, $translate, api, pipelineType) {
    angular.extend($scope, {
      common: {
        errors: []
      },
      selectedSource: '',
      selectedProcessors: {},
      selectedTargets: {},
      newConfig : {
        name: '',
        description: '',
        pipelineType: pipelineType !== undefined ? pipelineType: 'DATA_COLLECTOR'
      },

      save : function () {
        if($scope.newConfig.name) {
          api.pipelineAgent.createNewPipelineConfig(
            $scope.newConfig.name,
            $scope.newConfig.description,
            $scope.newConfig.pipelineType
          ).then(
            function(res) {
              $modalInstance.close(res.data);
            },
            function(res) {
              $scope.common.errors = [res.data];
            }
          );
        } else {
          $translate('home.library.nameRequiredValidation').then(function(translation) {
            $scope.common.errors = [translation];
          });
        }
      },
      cancel : function () {
        $modalInstance.dismiss('cancel');
      }
    });

  });
