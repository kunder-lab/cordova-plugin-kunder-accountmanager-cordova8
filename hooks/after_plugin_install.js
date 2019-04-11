#!/usr/bin/env node
'use strict';

var fs = require('fs');

module.exports = function(context) {

	var cordova_util = context.requireCordovaModule('cordova-lib/src/cordova/util'),
	ConfigParser = context.requireCordovaModule('cordova-common/src/ConfigParser/ConfigParser'),
	projectRoot = cordova_util.isCordova(),
	xml = cordova_util.projectConfig(projectRoot),
	cfg = new ConfigParser(xml),
	label = cfg.getPreference('AccountManagerLabel'),
	iconUrl = cfg.getPreference('AccountManagerIconUrl'),
	accountType = cfg.getPreference('AccountManagerType');

	if (iconUrl != undefined && iconUrl != '')
		fs.writeFileSync('platforms/android/app/src/main/res/drawable/am_icon.png', fs.readFileSync(iconUrl));

	var authenticatorFile = fs.readFileSync('platforms/android/app/src/main/res/xml/authenticator.xml','utf8');
	authenticatorFile = authenticatorFile.replace(/android:icon="[ \S]*"/i, 'android:icon="@drawable/am_icon"');
	authenticatorFile = authenticatorFile.replace(/android:smallIcon="[ \S]*"/i, 'android:smallIcon="@drawable/am_icon"');
	authenticatorFile = authenticatorFile.replace(/android:accountType="[ \S]*"/i, 'android:accountType="'+accountType+'"');
	fs.writeFileSync('platforms/android/app/src/main/res/xml/authenticator.xml', authenticatorFile);

	var stringFile = fs.readFileSync('platforms/android/app/src/main/res/values/strings.xml','utf8');

	fs.writeFileSync('platforms/android/app/src/main/res/values/strings.xml', stringFile);
};
