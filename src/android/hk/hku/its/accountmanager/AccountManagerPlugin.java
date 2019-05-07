package hk.hku.its.accountmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Build;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import com.scottyab.aescrypt.AESCrypt;
import java.security.GeneralSecurityException;

public class AccountManagerPlugin extends CordovaPlugin {

  private static final String TAG = "AccountManagerPlugin";
  private static String ENCRYPTION_KEY = null;

  public AccountManagerPlugin() {

  }

	private String decrypt(String value) throws GeneralSecurityException {
		String decryptedValue = "";
		decryptedValue = AESCrypt.decrypt(ENCRYPTION_KEY, value);
		return decryptedValue;
	}

	private String encrypt(String value) throws GeneralSecurityException {
		String encryptedValue = "";
		encryptedValue = AESCrypt.encrypt(ENCRYPTION_KEY, value);
		return encryptedValue;
	}

	private String getData(AccountManager am, Account account, String key) throws GeneralSecurityException {
		String data = "";
		data = decrypt(am.getUserData(account, encrypt(key)));
		return data;
	}

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    AccountManager accountManager = AccountManager.get(cordova.getActivity().getApplicationContext());

    // no ENCRYPTION_KEY
    if (!action.equals("initWithKey") && ENCRYPTION_KEY == null) {
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "No ENCRYPTION_KEY, please specify one by calling initWithKey(key, onSuccess, onError)"));
    }
    // set ENCRYPTION_KEY
    else if (action.equals("initWithKey")) {
      String encryptionKey = args.getString(0);

      ENCRYPTION_KEY = encryptionKey;
      JSONObject r = new JSONObject();
      r.put("result", "OK");
      r.put("result_message", "ENCRYPTION_KEY is set");
      callbackContext.success(r);
    }
    // add an account
    else if (action.equals("addAccount")) {
      String accountName = args.getString(0);
      String token = args.getString(1);
      String accountType = args.getString(2);
      Bundle userData = new Bundle();
      String scope = "";

      // encrypt both key and value in rawUserData
      try {
        JSONObject rawUserData = args.getJSONObject(4);
        Iterator<?> iterator = rawUserData.keys();
        while (iterator.hasNext()) {
          String key = (String)iterator.next();
          String value = rawUserData.get(key).toString();
          // get the scope from rawUserData
          if ("scope".equals(key))
            scope = value;
          key = encrypt(key);
          value = encrypt(value);
          userData.putString(key, value);
        }
        token = encrypt(token);
      } catch (JSONException je) {
        je.printStackTrace();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "userData parsing error: " + je.getMessage()));
      } catch (GeneralSecurityException gse) {
        gse.printStackTrace();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "userData or token encryption error: " + gse.getMessage()));
  		}

      // no scope found
      if ("".equals(scope))
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Scope is not defined in userData"));

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        Account account = new Account(accountName, accountType);
        if (accountManager.addAccountExplicitly(account, token, userData)) {
          JSONObject r = new JSONObject();
          r.put("result", "OK");
          r.put("result_message", "New account type and account added");
          callbackContext.success(r);
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to add new accountType"));
        }
      } else {
        // existing account found in accountType
        int i = 0;
        for (Account account : accounts) {
          // same scope has been registered before in accountType
          try {
            if (scope.equals(getData(accountManager, account, "scope"))) {
              for (String key : userData.keySet()) {
                String value = userData.getString(key);
                accountManager.setUserData(account, key, value);
              }
              accountManager.setPassword(account, token);
              JSONObject r = new JSONObject();
              r.put("result", "OK");
              r.put("result_message", "Account updated");
              callbackContext.success(r);
              i++;
              break;
            }
          } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Account data decryption error: " + gse.getMessage()));
          }
        }
        // the scope has not been registered before in accountType
        if (i == 0) {
          Account account = new Account(accountName, accountType);
          if (accountManager.addAccountExplicitly(account, token, userData)) {
            JSONObject r = new JSONObject();
            r.put("result", "OK");
            r.put("result_message", "New account added");
            callbackContext.success(r);
          } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to add account in accountType"));
          }
        }
      }
    }
    // remove an account
    else if (action.equals("removeAccount")) {
      String accountType = args.getString(0);
      String scope = args.getString(1);

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        JSONObject r = new JSONObject();
        r.put("result", "OK");
        r.put("result_message", "No account to remove");
        callbackContext.success(r);
      } else {
        int i = 0;
        for (Account account : accounts) {
          try {
            if (scope.equals(getData(accountManager, account, "scope"))) {
              // account found for scope, reset token
              accountManager.setPassword(account, "");
              // remove account
              if (Build.VERSION.SDK_INT >= 22) {
                if (accountManager.removeAccountExplicitly(account)) {
                  JSONObject r = new JSONObject();
                  r.put("result", "OK");
                  r.put("result_message", "Account removed");
                  callbackContext.success(r);
                } else {
                  callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to remove account, SDK: " + Build.VERSION.SDK_INT));
                }
              } else {
                //Deprecated on API 22
                accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                  @Override
                  public void run(AccountManagerFuture<Boolean> future) {
                    try {
                      if (future.getResult()) {
                        JSONObject r = new JSONObject();
                        r.put("result", "OK");
                        r.put("result_message", "Account removed");
                        callbackContext.success(r);
                      }
                    } catch (AuthenticatorException ae) {
                      ae.printStackTrace();
                      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to remove account, SDK: " + Build.VERSION.SDK_INT + ", error: " + ae.getMessage()));
                    } catch (OperationCanceledException oce) {
                      oce.printStackTrace();
                      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to remove account, SDK: " + Build.VERSION.SDK_INT + ", error: " + oce.getMessage()));
                    } catch (IOException ioe) {
                      ioe.printStackTrace();
                      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to remove account, SDK: " + Build.VERSION.SDK_INT + ", error: " + ioe.getMessage()));
                    } catch (JSONException je) {
                      je.printStackTrace();
                      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Fail to remove account, SDK: " + Build.VERSION.SDK_INT + ", error: " + je.getMessage()));
                    }
                  }
                }, null);
              }
              i++;
              break;
            }
          } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Account data decryption error: " + gse.getMessage()));
          }
        }
        if (i == 0) {
          JSONObject r = new JSONObject();
          r.put("result", "OK");
          r.put("result_message", "No account to remove");
          callbackContext.success(r);
        }
      }
    }
    // get account details
    else if (action.equals("getUserAccount")) {
      String accountType = args.getString(0);
      String scope = args.getString(2);

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found"));
      } else {
        for (Account account : accounts) {
          try {
            if (scope.equals(getData(accountManager, account, "scope"))) {
              // account found for scope
              JSONObject r = new JSONObject();
              r.put("name", account.name);
              r.put("uid", getData(accountManager, account, "uid"));
              r.put("univ_num", getData(accountManager, account, "univ_num"));
              r.put("scope", getData(accountManager, account, "scope"));
              r.put("token", decrypt(accountManager.getPassword(account)));
              callbackContext.success(r);
              break;
            }
          } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Account data decryption error: " + gse.getMessage()));
          }
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Scope not found in accountType"));
      }
    }
    // get account password
    else if (action.equals("getPassword")) {
      String accountType = args.getString(0);
      String scope = args.getString(2);

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found"));
      } else {
        for (Account account : accounts) {
          try {
            if (scope.equals(getData(accountManager, account, "scope"))) {
              JSONObject r = new JSONObject();
              r.put("token", decrypt(accountManager.getPassword(account)));
              callbackContext.success(r);
              break;
            }
          } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Account data decryption error: " + gse.getMessage()));
          }
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Scope not found in accountType"));
      }
    }
    else if (action.equals("getDataFromKey")) {
      String accountType = args.getString(0);
      String keyData = args.getString(2);
      Account [] accounts = accountManager.getAccountsByType(accountType);
      if (accounts.length == 0) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
      } else {
        String encryptedKey;
        try {
          encryptedKey = AESCrypt.encrypt(ENCRYPTION_KEY, keyData);
        } catch (GeneralSecurityException e) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
          return false;
        }
        String data = accountManager.getUserData(accounts[0], encryptedKey);
        if (data != null) {
          try { 
            data = AESCrypt.decrypt(ENCRYPTION_KEY, data);
          } catch (GeneralSecurityException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return false;
          }
          JSONObject r = new JSONObject();
          r.put(keyData, data);
          callbackContext.success(r);
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        }
      }
    }
    // set account details
    else if (action.equals("setUserData")) {
      String accountType = args.getString(0);
      Bundle userData = new Bundle();
      String scope = "";

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found"));
      } else {
        // encrypt both key and value in rawUserData
        try {
          JSONObject rawUserData = args.getJSONObject(2);
          Iterator<?> iterator = rawUserData.keys();
          while (iterator.hasNext()) {
            String key = (String)iterator.next();
            String value = rawUserData.get(key).toString();
            // get the scope from userData
            if ("scope".equals(key))
              scope = value;
            key = encrypt(key);
            value = encrypt(value);
            userData.putString(key, value);
          }
        } catch (JSONException je) {
          je.printStackTrace();
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "userData parsing error: " + je.getMessage()));
        } catch (GeneralSecurityException gse) {
          gse.printStackTrace();
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "userData encryption error: " + gse.getMessage()));
        }

        // no scope found
        if ("".equals(scope))
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Scope is not defined in rawUserData"));

        int i = 0;
        for (Account account : accounts) {
          // same scope has been registered before in accountType
          try {
            if (scope.equals(getData(accountManager, account, "scope"))) {
              for (String key : userData.keySet()) {
                String value = userData.getString(key);
                accountManager.setUserData(account, key, value);
              }
              JSONObject r = new JSONObject();
              r.put("result", "OK");
              r.put("result_message", "Account data updated");
              callbackContext.success(r);
              i++;
              break;
            }
          } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Account data decryption error: " + gse.getMessage()));
          }
        }
        // the scope has not been registered before in accountType
        if (i == 0) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found for scope"));
        }
      }
    }
    // change account password
    else if (action.equals("changePassword")) {
      String accountType = args.getString(0);
      String scope = args.getString(2);
      String newPassword = args.getString(3);

      Account [] accounts = accountManager.getAccountsByType(accountType);

      if (accounts.length == 0) {
        // no account exists in accountType
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found"));
      } else {
        // existing account found in accountType
        int i = 0;
        try {
          scope = encrypt(scope);
          newPassword = encrypt(newPassword);
          for (Account account : accounts) {
            // same scope has been registered before in accountType
            if (scope.equals(getData(accountManager, account, "scope"))) {
              accountManager.setPassword(account, newPassword);
              JSONObject r = new JSONObject();
              r.put("result", "OK");
              r.put("result_message", "Password updated");
              callbackContext.success(r);
              i++;
              break;
            }
          }
        } catch (GeneralSecurityException gse) {
          gse.printStackTrace();
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Password decryption error: " + gse.getMessage()));
        }
        // the scope has not been registered before in accountType
        if (i == 0) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, "Account not found for scope"));
        }
      }
    }
    return true;
  }
}
