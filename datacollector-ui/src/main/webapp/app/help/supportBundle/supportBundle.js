/**
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
 * Controller for Settings Modal Dialog.
 */

angular
    .module('dataCollectorApp')
    .controller('SupportBundleModalInstanceController', function ($scope, $rootScope, $modalInstance, $window, api) {

  $scope.showLoading = true;
  api.system.getSupportBundleGenerators().then(function(res) {
    $scope.showLoading = false;
    $scope.generators = _.map(res.data, function(generator) {
      generator.checked = generator.enabledByDefault;
      return generator;
    });
  }, function(res) {
    $scope.showLoading = false;
    $scope.common.errors = [res.data];
  });

  angular.extend($scope, {
    hasAnyGeneratorSelected: function() {
      return _.any($scope.generators, function(generator) {
        return generator.checked;
      });
    },

    getGenerateUrl: function() {
      if (!this.hasAnyGeneratorSelected()) {
        return '';
      }
      var selectedGenerators = _.filter($scope.generators, function(generator) {
        return generator.checked;
      });
      var fullyQualifiedClassNames = _.pluck(selectedGenerators, 'klass');
      var simpleClassNames = _.map(fullyQualifiedClassNames, function(className) {
        return _.last(className.split('.'));
      });
      return api.system.getGenerateSupportBundleUrl(simpleClassNames);
    },

    done: function() {
      $modalInstance.dismiss('cancel');
    },
  });
});
