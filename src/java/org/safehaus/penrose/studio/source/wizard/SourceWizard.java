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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCPrimaryKeyWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCTableWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCFieldWizardPage;
import org.safehaus.penrose.studio.ldap.source.LDAPTreeWizardPage;
import org.safehaus.penrose.studio.ldap.source.LDAPAttributeWizardPage;
import org.safehaus.penrose.studio.ldap.source.LDAPFieldWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.source.JDBCSource;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public SelectConnectionWizardPage connectionPage;

    public JDBCTableWizardPage jdbcTablePage;
    public JDBCFieldWizardPage jdbcFieldsPage;
    public JDBCPrimaryKeyWizardPage jdbcPrimaryKeyPage;

    public LDAPTreeWizardPage jndiTreePage;
    public LDAPAttributeWizardPage jndiAttributesPage;
    public LDAPFieldWizardPage jndiFieldsPage;

    public SourceWizard(String partitionName) throws Exception {
        this.partitionName = partitionName;

        setWindowTitle("New Source");
    }

    public void addPages() {

        propertyPage = new SourceWizardPage();

        addPage(propertyPage);

        connectionPage = new SelectConnectionWizardPage(partitionName);
        connectionPage.setProject(server);

        addPage(connectionPage);

        jdbcTablePage = new JDBCTableWizardPage();
        jdbcTablePage.setServer(server);
        jdbcTablePage.setPartitionName(partitionName);

        addPage(jdbcTablePage);

        jdbcFieldsPage = new JDBCFieldWizardPage();
        jdbcFieldsPage.setServer(server);
        jdbcFieldsPage.setPartitionName(partitionName);

        addPage(jdbcFieldsPage);

        jdbcPrimaryKeyPage = new JDBCPrimaryKeyWizardPage();

        addPage(jdbcPrimaryKeyPage);

        jndiTreePage = new LDAPTreeWizardPage();

        addPage(jndiTreePage);

        jndiAttributesPage = new LDAPAttributeWizardPage();

        addPage(jndiAttributesPage);

        jndiFieldsPage = new LDAPFieldWizardPage();

        addPage(jndiFieldsPage);
    }

    public boolean canFinish() {

        if (!propertyPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;

        ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
        if (connectionConfig == null) return false;

        String adapterName = connectionConfig.getAdapterName();

        if ("JDBC".equals(adapterName)) {
            if (!jdbcTablePage.isPageComplete()) return false;
            if (!jdbcFieldsPage.isPageComplete()) return false;
            if (!jdbcPrimaryKeyPage.isPageComplete()) return false;

        } else if ("LDAP".equals(adapterName)) {
            if (!jndiTreePage.isPageComplete()) return false;
            if (!jndiAttributesPage.isPageComplete()) return false;
            if (!jndiFieldsPage.isPageComplete()) return false;
        }

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;

            String adapterName = connectionConfig.getAdapterName();

            if ("JDBC".equals(adapterName)) {
                jdbcTablePage.setConnectionConfig(connectionConfig);
                return jdbcTablePage;

            } else if ("LDAP".equals(adapterName)) {
                jndiTreePage.setConnectionConfig(connectionConfig);
                return jndiTreePage;

            } else {
                return null;
            }

        } else if (jdbcTablePage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            Table table = jdbcTablePage.getTable();
            jdbcFieldsPage.setTableConfig(connectionConfig, table);

        } else if (jdbcFieldsPage == page) {
            Collection<FieldConfig> selectedFields = jdbcFieldsPage.getSelectedFieldConfigs();
            jdbcPrimaryKeyPage.setFieldConfigs(selectedFields);

        } else if (jdbcPrimaryKeyPage == page) {
            return null;

        } else if (jndiTreePage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            jndiAttributesPage.setConnectionConfig(connectionConfig);

        } else if (jndiAttributesPage == page) {
            Collection<AttributeType> attributeTypes = jndiAttributesPage.getAttributeTypes();
            jndiFieldsPage.setAttributeTypes(attributeTypes);
        }

        return super.getNextPage(page);
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
        if (jndiTreePage == page) {
            return connectionPage;
        }

        return super.getPreviousPage(page);
    }

    public boolean performFinish() {
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return false;

            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            String adapterName = connectionConfig.getAdapterName();
            if ("JDBC".equals(adapterName)) {
                Table table = jdbcTablePage.getTable();

                String catalog   = table.getCatalog();
                String schema    = table.getSchema();
                String tableName = table.getName();

                sourceConfig.setParameter(JDBCSource.CATALOG, catalog);
                sourceConfig.setParameter(JDBCSource.SCHEMA, schema);
                sourceConfig.setParameter(JDBCSource.TABLE, tableName);

                String filter = jdbcFieldsPage.getFilter();
                if (filter != null) {
                    sourceConfig.setParameter(JDBCSource.FILTER, filter);
                }

                Collection<FieldConfig> fields = jdbcPrimaryKeyPage.getFields();
                if (fields.isEmpty()) {
                    fields = jdbcFieldsPage.getSelectedFieldConfigs();
                }

                for (FieldConfig fieldConfig : fields) {
                    sourceConfig.addFieldConfig(fieldConfig);
                }

            } else if ("LDAP".equals(adapterName)) {
                sourceConfig.setParameter("baseDn", jndiTreePage.getBaseDn());
                sourceConfig.setParameter("filter", jndiTreePage.getFilter());
                sourceConfig.setParameter("scope", jndiTreePage.getScope());
                sourceConfig.setParameter("objectClasses", jndiTreePage.getObjectClasses());

                Collection<FieldConfig> fields = jndiFieldsPage.getFields();
                for (FieldConfig fieldConfig : fields) {
                    sourceConfig.addFieldConfig(fieldConfig);
                }

            }
/*
            SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
            sourceConfigManager.addSourceConfig(sourceConfig);
            project.save(partitionConfig, sourceConfigManager);
*/
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);
            partitionClient.store();
            
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig connection) {
        this.sourceConfig = connection;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
