package com.staticfloat.ihavecontrol;

import android.content.Context;
import java.util.*;
import android.view.*;
import android.widget.*;

public class SSHConfAdapter extends BaseAdapter {
    private final Context context;

    // This is the list of objects that conform to the CCItem interface, all of which we can draw
    private List<SSHConfiguration> items;

    public SSHConfAdapter( Context context ) {
        this.context = context;
        this.items = new LinkedList<SSHConfiguration>();
    }

    public SSHConfAdapter( Context context, Map<String,SSHConfiguration> items ) {
        this.context = context;
        update( items );
    }

    public void update( Map<String,SSHConfiguration> items ) {
        this.items = new LinkedList<SSHConfiguration>( items.values() );
        Collections.sort(this.items);
        notifyDataSetChanged();
    }

    public void clear() {
        this.items = new LinkedList<SSHConfiguration>();
        notifyDataSetChanged();
    }

    public SSHConfiguration getItem( int position ) {
        return items.get(position);
    }

    public long getItemId( int position ) {
        return position;
    }

    public void addItem( SSHConfiguration item ) {
        items.add(item);
        Collections.sort(items);

        notifyDataSetChanged();
    }

    public void insertItem(int position, SSHConfiguration item ) {
        items.add(position, item);
        Collections.sort(items);

        notifyDataSetChanged();
    }

    public void delItem(int position) {
        items.remove(position);
        Collections.sort(items);

        notifyDataSetChanged();
    }

    protected void onClick( int position) {
        SSHConfiguration sshconf = this.getItem(position);
        (new SSHCommand(context)).execute(sshconf);
    }

    public View getView( int position, View view, ViewGroup parent ) {
        //Get a layout inflater that is already up and running
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // Get the item we're going to be rendering
        SSHConfiguration item = items.get(position);

        // Check to see if we need to inflate a new view, or if the one we already have will work
        if( view == null ) {
            //Load the xml for this item, inflate it into a view, and don't attach it to parent yet
            view = inflater.inflate(R.layout.fragment_sshconf_row, parent, false);
        }

        String auth_text = item.getUser() + "@" + item.getServer() + ":" + item.getPort();
        ((TextView) view.findViewById(R.id.title)).setText( item.getName() );
        ((TextView) view.findViewById(R.id.auth)).setText( auth_text );
        return view;
    }

    public int getCount() {
        return items.size();
    }
}