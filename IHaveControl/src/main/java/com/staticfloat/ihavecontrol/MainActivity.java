package com.staticfloat.ihavecontrol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.*;

import static android.app.backup.BackupManager.dataChanged;

public class MainActivity extends Activity {
    // The list of SSH configurations we have saved
    Map<String, SSHConfiguration> sshconfs;
    public final int EDIT_SSH_CONF_ACTIVITY_ID = 5040;

    // This is the SSHConf Adapter used in onCreate:
    SSHConfAdapter sca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sshconfs = loadSSHConfs();
        // Create the adapter that will manage the SSHConfigurations
        sca = new SSHConfAdapter( this, sshconfs );
        ListView lv = ((ListView)findViewById(R.id.listView));
        lv.setAdapter(sca);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sca.onClick(i);
            }
        });

        registerForContextMenu(lv);
    }

    protected void log(String msg) {
        Log.d("com.staticfloat.ihavecontrol", msg);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.listView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.sshconf_longclick, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.ssh_conf_connect:
                sca.onClick(info.position);
                return true;
            case R.id.ssh_conf_edit:
                Intent intent = new Intent(getBaseContext(), EditSSHConfActivity.class);
                intent.putExtra("SSHConf", sca.getItem(info.position));
                startActivityForResult(intent, EDIT_SSH_CONF_ACTIVITY_ID);
                return true;
            case R.id.ssh_conf_delete:
                SSHConfiguration del_sshconf = sca.getItem(info.position);
                sca.delItem(info.position);

                this.sshconfs.remove(del_sshconf.getName());
                saveSSHConfs();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // Gets called when we're done with the EditSSHConfActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        switch( requestCode ) {
            case EDIT_SSH_CONF_ACTIVITY_ID: {
                if( resultCode == RESULT_OK ) {
                    SSHConfiguration new_sshconf = (SSHConfiguration)data.getExtras().get("SSHConf");
                    this.sshconfs.put(new_sshconf.getName(), new_sshconf);
                    saveSSHConfs();
                    this.sca.update(this.sshconfs);

                    // Tell Android we need to have our data backed up!
                    (new BackupManager(this)).dataChanged();
                    log("Requesting backup...");
                }
            }

        }
    }

    public Map<String, SSHConfiguration> getSSHConfs() {
        return this.sshconfs;
    }

    protected Map<String, SSHConfiguration> loadSSHConfs() {
        Map<String, SSHConfiguration> sshconfs = new TreeMap<String, SSHConfiguration>();
        // Try to load SSHConfigurations from disk.  If we can't, don't do anything
        try {
            FileInputStream fis = openFileInput("ssh_confs");
            ObjectInputStream ois  = new ObjectInputStream(fis);
            Map<String, SSHConfiguration> saved_sshconfs = (Map<String, SSHConfiguration>) ois.readObject();
            sshconfs.putAll(saved_sshconfs);
        } catch (FileNotFoundException e) {
            log("Unable to open \"ssh_confs\"");
            e.printStackTrace();
        } catch (IOException e) {
            log("\"ssh_confs\" stream unreadable!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log("ClassNotFoundException within loadSSHConfs()!");
            e.printStackTrace();
        }

        return sshconfs;
    }

    protected void saveSSHConfs() {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("ssh_confs", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.sshconfs);
            oos.close();
        } catch (FileNotFoundException e) {
            log("FileNotFound exception in saveSSHConfs()!");
            e.printStackTrace();
        } catch (IOException e) {
            log("Could not write out to ssh_confs!");
            e.printStackTrace();
        }
    }

    protected void clearSSHConfs() {
        try {
            FileOutputStream fos = openFileOutput("ssh_confs", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
        }
        sca.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.clear_ssh_confs:
                clearSSHConfs();
                return true;
            case R.id.add_ssh_confs:
                Intent intent = new Intent(getBaseContext(), EditSSHConfActivity.class);
                startActivityForResult(intent, EDIT_SSH_CONF_ACTIVITY_ID);
                return true;
            case R.id.auto_populate:
                // This is my own cheap way of generating configurations for easy testing
                SSHConfiguration nova_wake = new SSHConfiguration("[nova wake]", "root", "creep.e.ip.saba.us", 22, "wol -i 10.10.11.0 d4:3d:7e:04:69:1e" );
                nova_wake.setKeyfileAuth("/sdcard/id_rsa");
                sshconfs.put("[nova wake]", nova_wake);

                SSHConfiguration nova_kill = new SSHConfiguration("[nova kill]", "sabae", "nova.e.ip.saba.us", 9022, "sudo poweroff; echo Shutting nova down..." );
                nova_kill.setKeyfileAuth("/sdcard/id_rsa");
                sshconfs.put("[nova kill]", nova_kill);

                sca.update(sshconfs);
                saveSSHConfs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
