package com.staticfloat.ihavecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;

public class EditSSHConfActivity extends Activity {
    SSHAuth saved_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsshconf);

        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            SSHConfiguration sshconf = (SSHConfiguration)extras.get("SSHConf");
            if( sshconf != null ) {
                putSSHConf(sshconf);
                saved_auth = sshconf.getAuth();
            }
        }

        ((RadioGroup)findViewById(R.id.radioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                EditText privkeyEdit = ((EditText)findViewById(R.id.privkeyEdit));
                EditText passwordEdit = ((EditText)findViewById(R.id.passwordEdit));
                if( id == R.id.privkeyButton ) {
                    passwordEdit.setEnabled(false);
                    passwordEdit.setClickable(false);
                    privkeyEdit.setEnabled(true);
                    privkeyEdit.setClickable(true);
                    verifyPrivKey();
                } else {
                    privkeyEdit.setEnabled(false);
                    privkeyEdit.setClickable(false);
                    passwordEdit.setEnabled(true);
                    passwordEdit.setClickable(true);
                }
            }
        });

        ((EditText)findViewById(R.id.privkeyEdit)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            public void afterTextChanged(Editable editable) {
                verifyPrivKey();
            }
        });

        ((EditText)findViewById(R.id.privkeyEdit)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                verifyPrivKey();
                return false;
            }
        });
    }

    public void saveAndReturn(View v) {
        Intent data = new Intent();
        data.putExtra("SSHConf", getSSHConf());
        setResult(RESULT_OK, data);
        finish();
    }

    protected void verifyPrivKey() {
        String privkey_path = ((EditText)findViewById(R.id.privkeyEdit)).getText().toString();
        File privkey = new File(privkey_path);
        if( (saved_auth instanceof SSHKeyfileAuth && privkey_path.length() == 0) ||
            (privkey.exists() && privkey.isFile()) )
        {
            ((Button)findViewById(R.id.saveButton)).setEnabled(true);
            ((Button)findViewById(R.id.saveButton)).setClickable(true);
        } else {
            ((Button)findViewById(R.id.saveButton)).setEnabled(false);
            ((Button)findViewById(R.id.saveButton)).setClickable(false);
        }
    }

    protected void putSSHConf( SSHConfiguration sshconf ) {
        ((EditText)findViewById(R.id.nameEdit)).setText(sshconf.getName());
        ((EditText)findViewById(R.id.userEdit)).setText(sshconf.getUser());
        ((EditText)findViewById(R.id.serverEdit)).setText(sshconf.getServer());
        ((EditText)findViewById(R.id.portEdit)).setText(""+sshconf.getPort());
        ((EditText)findViewById(R.id.cmdEdit)).setText(sshconf.getCommand());

        EditText privkeyEdit = ((EditText)findViewById(R.id.privkeyEdit));
        EditText passwordEdit = ((EditText)findViewById(R.id.passwordEdit));
        if( sshconf.isPasswordAuth() ) {
            ((RadioButton)findViewById(R.id.passwordButton)).setChecked(true);
            passwordEdit.setText(sshconf.getPassword());
            passwordEdit.setEnabled(true);
            passwordEdit.setClickable(true);
            privkeyEdit.setEnabled(false);
            privkeyEdit.setClickable(false);
        } else {
            ((RadioButton)findViewById(R.id.privkeyButton)).setChecked(true);
            privkeyEdit.setHint("<loaded private key>");
            privkeyEdit.setEnabled(true);
            privkeyEdit.setClickable(true);
            passwordEdit.setEnabled(false);
            passwordEdit.setClickable(false);
        }
    }

    protected SSHConfiguration getSSHConf() {
        String name = ((EditText)findViewById(R.id.nameEdit)).getText().toString();
        String user = ((EditText)findViewById(R.id.userEdit)).getText().toString();
        String server = ((EditText)findViewById(R.id.serverEdit)).getText().toString();
        String port = ((EditText)findViewById(R.id.portEdit)).getText().toString();
        String cmd = ((EditText)findViewById(R.id.cmdEdit)).getText().toString();

        SSHConfiguration sshconf = new SSHConfiguration(name, user, server, Integer.parseInt(port), cmd );
        if( ((RadioButton)findViewById(R.id.passwordButton)).isChecked() ) {
            sshconf.setPasswordAuth( ((EditText)findViewById(R.id.passwordEdit)).getText().toString() );
        } else {
            String keyfile_path = ((EditText)findViewById(R.id.privkeyEdit)).getText().toString();
            if( keyfile_path.length() == 0 ) {
                sshconf.setKeyfileAuth((SSHKeyfileAuth)saved_auth);
            } else {
                System.out.println("Loading " + keyfile_path + " for " + name);
                sshconf.setKeyfileAuth( keyfile_path );
            }
        }

        return sshconf;
    }
}
