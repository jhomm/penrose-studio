/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.jdbc.source.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.JDBC;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourcePropertiesWizard extends SourceWizard {

    public SelectConnectionWizardPage connectionPage;
    public JDBCSourceTableWizardPage tablePage;
    public JDBCSourceFilterWizardPage filterPage;

    public JDBCSourcePropertiesWizard() {
        setWindowTitle("Edit JDBC Source Properties");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();
        
        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("JDBC");
        connectionPage.setConnectionName(sourceConfig.getConnectionName());

        addPage(connectionPage);

        tablePage = new JDBCSourceTableWizardPage();
        tablePage.setServer(server);
        tablePage.setPartitionName(partitionName);

        addPage(tablePage);

        filterPage = new JDBCSourceFilterWizardPage();

        addPage(filterPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!tablePage.isPageComplete()) return false;
        if (!filterPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            tablePage.setConnectionName(connectionConfig.getName());
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig.setConnectionName(connectionConfig.getName());

            Table table = tablePage.getTable();

            sourceConfig.setParameter(JDBC.CATALOG, table.getCatalog());
            sourceConfig.setParameter(JDBC.SCHEMA, table.getSchema());
            sourceConfig.setParameter(JDBC.TABLE, table.getName());

            sourceConfig.setParameter(JDBC.FILTER, filterPage.getFilter());

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.updateSource(sourceConfig.getName(), sourceConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }
}