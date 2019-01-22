/*global module, cordova */
'use strict';
module.exports = (function() {

  var _initWithKey = function(encryptionKey, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'initWithKey', [encryptionKey]);
  };

  var _addAccount = function(accountName, token, accountType, group, userData, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'addAccount', [accountName, token, accountType, group, userData]);
  };

  var _removeAccount = function(accountType, scope, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'removeAccount', [accountType, scope]);
  };

  var _getUserAccount = function(accountType, group, scope, successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'getUserAccount', [accountType, group, scope]);
  };

  var _getPassword = function(accountType, group, scope, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'getPassword', [accountType, group, scope]);
  };

  var _setUserData = function(accountType, group, data, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'setUserData', [accountType, group, data]);
  };

  var _changePassword = function(accountType, group, scope, newPassword, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'AccountManager', 'changePassword', [accountType, group, scope, newPassword]);
  };

  return {
    initWithKey: _initWithKey,
    addAccount: _addAccount,
    removeAccount: _removeAccount,
    getUserAccount: _getUserAccount,
    getPassword: _getPassword,
    setUserData: _setUserData,
    setPassword: _changePassword
  };

})();
