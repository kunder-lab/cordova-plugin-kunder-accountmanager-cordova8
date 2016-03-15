#!/usr/bin/env node
'use strict';

var fs = require('fs');

module.exports = function(context) {

	var cordova_util = context.requireCordovaModule('cordova-lib/src/cordova/util'),
	ConfigParser = context.requireCordovaModule('cordova-lib/src/configparser/ConfigParser'),
	projectRoot = cordova_util.isCordova(),
	xml = cordova_util.projectConfig(projectRoot),
	cfg = new ConfigParser(xml),
	label = cfg.getPreference('AccountManagerLabel'),
	iconUrl = cfg.getPreference('AccountManagerIconUrl'),
	accountType = cfg.getPreference('AccountManagerType');

	//Pasos a seguir
	//1.- Copiar el icono a la carpeta drawable del proyecto
	//2.- Modificar el archivo authenticator.xml y reemplazar android:icon="@drawable/here_your_icon" y android:smallIcon="@drawable/here_your_icon"
	//	  por android:icon="@drawable/acm_icon" y android:smallIcon="@drawable/acm_icon", respectivamente
	//3.- Reemplazar android:accountType="here.the.unique.package.identifier" por el account type leído del config.xml
	//4.- Reemplazar <string name="authLabel">name_to_show_in_account_settings</string> de res/values/string.xml por el label leído
	//	  del config.xml

	//1.-
	fs.writeFileSync('platforms/android/res/drawable/acm_icon.png', fs.readFileSync(iconUrl));

	//2 y 3
	var authenticatorFile = fs.readFileSync('platforms/android/res/xml/authenticator.xml','utf8');
	authenticatorFile = authenticatorFile.replace('android:icon="@drawable/here_your_icon"', 'android:icon="@drawable/acm_icon"');
	authenticatorFile = authenticatorFile.replace('android:smallIcon="@drawable/here_your_icon"', 'android:smallIcon="@drawable/acm_icon"');
	authenticatorFile = authenticatorFile.replace('android:accountType="here.the.unique.package.identifier"', 'android:accountType="'+accountType+'"');
	fs.writeFileSync('platforms/android/res/xml/authenticator.xml', authenticatorFile);

	//4.-
	var stringFile = fs.readFileSync('platforms/android/res/values/strings.xml','utf8');
	if(stringFile.indexOf('<string name="authLabel">') > -1){
		stringFile = stringFile.replace(/\<string name\=\"authLabel\"\>[\s\S]*\<\/string\>/i, '<string name="authLabel">'+label+'</string>');
	}
	else{
		stringFile = stringFile.replace('</resources>', '<string name="authLabel">'+label+'</string></resources>');
	}
	
	fs.writeFileSync('platforms/android/res/values/strings.xml', stringFile);
};
