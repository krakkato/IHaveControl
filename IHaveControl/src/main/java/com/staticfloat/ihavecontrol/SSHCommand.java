package com.staticfloat.ihavecontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;

public class SSHCommand extends AsyncTask<Object, String, String> {
    WeakReference<Context> myContext;

    public SSHCommand(Context myContext) {
        this.myContext = new WeakReference<Context>(myContext);
    }

    protected String doInBackground(Object... params) {
        SSHConfiguration sshconf = (SSHConfiguration)params[0];
        try
        {
            return sshconf.execute();
        } catch( Exception e ) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    public void setContext( Context myContext ) {
        this.myContext = new WeakReference<Context>(myContext);
    }

    protected void onPostExecute(String result) {
        if( myContext.get() != null )
            Toast.makeText( myContext.get(), result, Toast.LENGTH_LONG ).show();
    }
}
