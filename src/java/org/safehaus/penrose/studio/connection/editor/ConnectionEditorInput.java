package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;

/**
 * @author Endi S. Dewata
 */
public class ConnectionEditorInput implements IEditorInput {

    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;

    public ConnectionEditorInput() {
    }

    public ConnectionEditorInput(PartitionConfig partitionConfig, ConnectionConfig connectionConfig) {
        this.partitionConfig = partitionConfig;
        this.connectionConfig = connectionConfig;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return connectionConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return connectionConfig.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public int hashCode() {
        return (partitionConfig == null ? 0 : partitionConfig.hashCode()) +
                (connectionConfig == null ? 0 : connectionConfig.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof ConnectionEditorInput)) return false;

        ConnectionEditorInput cei = (ConnectionEditorInput)o;

        if (!equals(partitionConfig, cei.partitionConfig)) return false;
        if (!equals(connectionConfig, cei.connectionConfig)) return false;

        return true;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
