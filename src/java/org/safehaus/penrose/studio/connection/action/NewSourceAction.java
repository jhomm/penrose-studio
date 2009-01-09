/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.connection.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.ldap.source.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.connection.ConnectionNode;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;

public class NewSourceAction extends Action {

    Logger log = Logger.getLogger(getClass());

    ConnectionNode node;

	public NewSourceAction(ConnectionNode node) {
        this.node = node;

        setText("New Source...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server project = node.getProjectNode().getServer();

            String partitionName  = node.getPartitionName();
            String adapterName    = node.getAdapterName();
            String connectionName = node.getConnectionName();

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

            if ("JDBC".equals(adapterName)) {
                JDBCSourceWizard wizard = new JDBCSourceWizard();
                wizard.setServer(project);
                wizard.setPartitionName(partitionName);
                wizard.setConnectionConfig(connectionConfig);

                WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
                dialog.setPageSize(600, 300);
                dialog.open();

            } else if ("LDAP".equals(adapterName)) {
                LDAPSourceWizard wizard = new LDAPSourceWizard();
                wizard.setServer(project);
                wizard.setPartitionName(partitionName);
                wizard.setConnectionConfig(connectionConfig);

                WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
                dialog.setPageSize(600, 300);
                dialog.open();
            }

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
	}
	
}