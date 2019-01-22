<!---
license: Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# cordova-plugin-its-accountmanager

This cordova plugin enables you to use Android Account Manager and iOS Keychain to manage accounts of one user and share with other applications of the same company (e.g google apps like gmail, youtube, etc).

It's possible to moddify this plugins to allow multiple accounts.

## Installation

```
cordova plugin add https://github.com/kunder-lab/cordova-plugin-kunder-accountmanager-cordova8
```

## Account Manager settings

You need to add the following settings in your config.xml file for Android Account Manager to work:

```
<preference name="AccountManagerLabel" value="Your Label Here" />
<preference name="AccountManagerIconUrl" value="your_icon_url_here.png" />
<preference name="AccountManagerType" value="your.unique.identifier" />
```

## Supported Platforms

- Android
- iOS

## Methods

- registerAccount: register an user in Account Manager
- removeAccount: remove an account from Account Manager (Android) and remove all data from keychain (iOS)
- getUserAccount: returns an String with account name if account exist
- getPassword: returns password if account exist
- getDataFromKey: returns data from specified key
- setUserData: set object with information into Account Manager or Keychain
- setPassword: update account password
- resetPassword: update account password with String "0000"

## Key Parameters

- accountType: the same account type unique identifier set in config.xml file
- group (Nullable): group identifier. Only for iOS shared keychain
- data: key-value object

## Notes

- You can not delete user data from Account Manager. Use "setUserData" to change value to empty.
- You can delete account from Account Manager.
- removeAccount remove all keychain data from your app.
- You can not set user data to Account Manager if it doesn't have an account for your identifier.

## License

Apache License 2.0
