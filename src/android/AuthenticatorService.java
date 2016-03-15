package cl.kunder.accountmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by sebastian on 08-03-16.
 */
public class AuthenticatorService extends Service{
    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
