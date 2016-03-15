package cl.kunder.accountmanager;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by sebastian on 07-03-16.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    private Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        //Pasos a seguir:
        /* 1ero. Se debe verificar si existe una cuenta. Si existe, entonces se debe retornar un mensaje de error indicando que no es posible
        * crear 2 cuentas de ELU
        * 2do. Si no existe una cuenta, entonces se debe mostrar la aplicación en ionic, levantar un webview o algo parecido, y retornar el bundle con
        * el intent, o bien, sólo levantar la aplicación, ya que si no tiene una cuenta asociada (no hay enrolamiento) entonces debería mostrar
        * la vista de registro por defecto*/
        /*
        AccountManager accountManager = AccountManager.get(context);

        Account [] accounts = accountManager.getAccountsByType(accountType);
        if(accounts.length == 0){
            Hacer el proceso normal, abrir la aplicación
        }
        */
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        /*
        * Si existe el token (y la cuenta) retorna el token.
        * Caso contrario retorna addAccount*/
        /*
        AccountManager accountManager = AccountManager.get(context);
        String password = accountManager.getPassword(account);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if(!TextUtils.isEmpty(authToken)){
            //Caso de éxito, devuelve el token
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }
        */

        /*
        * Caso de authToken no exista, entonces implica que no se ha creado una credencial, debería retornar nulo? o se debería abrir la vista?
        * ¿Desde dónde se llama a este procedimiento?*/
        /*
        * */
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        /*
        * Se retorna el string con el nombre de la "aplicación" que se mostrará en el account manager del sistema*/
        return authTokenType;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
