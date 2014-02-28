package com.staticfloat.ihavecontrol;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;


public class ControlBackupAgent extends BackupAgentHelper {
    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        Log.d("com.staticfloat.ihavecontrol", "ControlBackupAgent.onCreate() called!");
        // Backup ssh_confs, so that we never lose anything.  EVER.  >:D
        FileBackupHelper helper = new FileBackupHelper(this, "ssh_confs");
        addHelper("ssh_confs", helper);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newstate) throws IOException {
        super.onRestore(data, appVersionCode, newstate);
        Log.d("com.staticfloat.ihavecontrol", "ControlBackupAgent.onRestore() called!");
    }
}