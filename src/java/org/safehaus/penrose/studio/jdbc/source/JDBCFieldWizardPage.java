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
package org.safehaus.penrose.studio.jdbc.source;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.safehaus.penrose.jdbc.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCFieldWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Database Fields";

    Table availableTable;
    Table selectedTable;

    Text filterText;

    private Server server;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private org.safehaus.penrose.jdbc.Table table;

    public JDBCFieldWizardPage() {
        super(NAME);

        setDescription("Select fields. Enter SQL filter (optional).");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        availableTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        availableTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setSize(50, 100);
        buttons.setLayout(new FillLayout(SWT.VERTICAL));

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText(">");
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (availableTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = availableTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                selectedTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(selectedTable, SWT.NONE);
                    item.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (selectedTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = selectedTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                availableTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(availableTable, SWT.NONE);
                    item.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                selectedTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(selectedTable, SWT.NONE);
                    item.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                availableTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(availableTable, SWT.NONE);
                    item.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        selectedTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite filterComposite = new Composite(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        filterComposite.setLayoutData(gd);
        filterComposite.setLayout(new GridLayout(2, false));

        Label filterLabel = new Label(filterComposite, SWT.NONE);
        filterLabel.setText("SQL Filter:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        filterText = new Text(filterComposite, SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(filterComposite, SWT.NONE);

        Label filterExample = new Label(filterComposite, SWT.NONE);
        filterExample.setText("Example: active = 'Y' and level < 5");

        setPageComplete(validatePage());
    }

    public void setTableConfig(ConnectionConfig connectionConfig, org.safehaus.penrose.jdbc.Table tableConfig) {
        this.connectionConfig = connectionConfig;
        this.table = tableConfig;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            String catalog = table.getCatalog();
            String schema = table.getSchema();
            String tableName = table.getName();

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionConfig.getName());

            //JDBCClient client = new JDBCClient(connectionConfig.getParameters());

            Collection<FieldConfig> fields = (Collection<FieldConfig>)connectionClient.invoke(
                    "getColumns",
                    new Object[] { catalog, schema, tableName },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() }
            );
            //Collection<FieldConfig> fields = client.getColumns(catalog, schema, tableName);
            //client.close();

            if (fields == null) return;

            Set<String> set = new HashSet<String>();
            TableItem items[] = selectedTable.getItems();
            for (TableItem item : items) {
                FieldConfig field = (FieldConfig) item.getData();
                set.add(field.getName());
            }

            availableTable.removeAll();

            for (FieldConfig field : fields) {
                if (set.contains(field.getName())) continue;

                TableItem item = new TableItem(availableTable, SWT.NONE);
                item.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                item.setText(field.getName());
                item.setData(field);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Collection<FieldConfig> getSelectedFieldConfigs() {
        Collection<FieldConfig> fieldConfigs = new ArrayList<FieldConfig>();
        TableItem items[] = selectedTable.getItems();
        for (TableItem item : items) {
            FieldConfig fieldConfig = (FieldConfig) item.getData();
            fieldConfigs.add(fieldConfig);
        }
        return fieldConfigs;
    }

    public String getFilter() {
        return "".equals(filterText.getText()) ? null : filterText.getText();
    }

    public boolean validatePage() {
        return getSelectedFieldConfigs().size() > 0;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public org.safehaus.penrose.jdbc.Table getTable() {
        return table;
    }

    public void setTable(org.safehaus.penrose.jdbc.Table table) {
        this.table = table;
    }
}
