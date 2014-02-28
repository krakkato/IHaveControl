package com.staticfloat.ihavecontrol;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.util.Properties;

public class SSHConfiguration implements Serializable, Comparable<SSHConfiguration> {
    protected String name;
    protected String user;
    protected String server;
    protected int port;
    protected SSHAuth auth;
    protected String command;

    public SSHConfiguration( String name, String user, String server, int port, String command) {
        this.name = name;
        this.user = user;
        this.server = server;
        this.port = port;
        this.command = command;
    }

    public int compareTo( SSHConfiguration other) {
        return this.name.compareTo(other.getName());
    }

    public void setPasswordAuth( String password ) {
        this.auth = new SSHPasswordAuth( password );
    }

    public void setKeyfileAuth( String keyfile_path ) {
        this.auth = new SSHKeyfileAuth( keyfile_path );
    }

    public void setKeyfileAuth( String keyfile_path, String passphrase ) {
        this.auth = new SSHKeyfileAuth( keyfile_path, passphrase );
    }

    public void setKeyfileAuth( SSHKeyfileAuth auth ) {
        this.auth = auth;
    }

    public String getName() {
        return this.name;
    }

    public String getUser() {
        return this.user;
    }

    public String getServer() {
        return this.server;
    }

    public int getPort() {
        return this.port;
    }

    public String getCommand() { return this.command; }

    boolean isPasswordAuth() { return this.auth instanceof SSHPasswordAuth; }

    public SSHAuth getAuth() { return this.auth; }

    public String getPassword() {
        if( !isPasswordAuth() )
            return "";
        return ((SSHPasswordAuth)this.auth).getPassword();
    }

    public String execute() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(this.user, this.server, this.port);
        this.auth.authenticate( this.getName(), jsch, session );
        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        // Connect and send command
        session.connect();
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        InputStream is = channel.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        channel.connect();

        // We use a StringBuilder for efficiency!
        StringBuilder ret = new StringBuilder(this.name + ": ");

        // Read in bytes into ret until channel.isClosed() is true
        byte[] tmp = new byte[1024];
        String line;
        while( (line = br.readLine()) != null || !channel.isClosed() ) {
            if( line != null )
                ret.append(line);
            else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
        // Cleanup
        is.close();
        channel.disconnect();
        session.disconnect();
        return ret.toString();
    }
}


// These classes are my little toy usage of inheritance to solve password and keyfile authentication
abstract class SSHAuth implements Serializable {
    public abstract void authenticate( String name, JSch j, Session s );
}

class SSHPasswordAuth extends SSHAuth implements Serializable {
    protected String password;

    public SSHPasswordAuth( String password ) {
        this.password = password;
    }

    public void authenticate( String name, JSch j, Session s ) {
        s.setPassword(password);
    }

    public String getPassword() {
        return this.password;
    }
}

class SSHKeyfileAuth extends SSHAuth implements Serializable {
    // The contents of the private_key
    protected byte[] private_key;
    // The passphrase needed to unlock the private_keyfile, null if not needed
    protected byte[] passphrase;

    public SSHKeyfileAuth( String keyfile_path ) {
        this(keyfile_path, "");
    }

    public SSHKeyfileAuth( String keyfile_path, String passphrase ){
        // First, read in keyfile_path, so we have it stored
        File f = new File(keyfile_path);
        try {
            FileInputStream fis = new FileInputStream(f);
            this.private_key = new byte[(int)f.length()];
            int bytes_read = fis.read(this.private_key, 0, (int) f.length());
            if( bytes_read != f.length() )
                System.out.println("bytes_read != f.length()!");
            fis.close();

            if( passphrase.equals("") )
                this.passphrase = (byte [])null;
            else
                this.passphrase = passphrase.getBytes("UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authenticate( String name, JSch j, Session s ) {
        try {
            j.addIdentity( name, this.private_key, (byte [])null, this.passphrase );
        } catch (JSchException e) {
            System.out.println("ERROR: Could not create Identity!");
            e.printStackTrace();
        }
    }
}