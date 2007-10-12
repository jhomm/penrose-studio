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
package org.safehaus.penrose.studio.project;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

public class ProjectDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int SAVE   = 1;

    Shell shell;

	Text nameText;
    Combo typeCombo;
	Text hostText;
    Text portText;
	Text bindDnText;
	Text bindPasswordText;

	private ProjectConfig projectConfig;
	
    private int action;

	public ProjectDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open() {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGO16));

        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createForm(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = createButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        composite.setLayoutData(gd);
    }

    public Composite createForm(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(PenroseClient.PENROSE);
        typeCombo.add(PenroseClient.JBOSS);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Host:");

		hostText = new Text(composite, SWT.BORDER);
        hostText.setText("localhost");
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText("Port:");

        portText = new Text(composite, SWT.BORDER);
        portText.setText("1099");
        portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label usernameLabel = new Label(composite, SWT.NONE);
        usernameLabel.setText("Bind DN:");

		bindDnText = new Text(composite, SWT.BORDER);
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

		bindPasswordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(composite, SWT.NONE);

		Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					PenroseClient client = new PenroseClient(
                            typeCombo.getText(),
                            hostText.getText(),
                            "".equals(portText.getText()) ? 0 : Integer.parseInt(portText.getText()),
                            bindDnText.getText(),
                            bindPasswordText.getText());
					client.connect();
                    client.close();
					MessageDialog.openInformation(shell, "Test Connection Result", "Connect Successful!");
                    
				} catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
					MessageDialog.openError(shell, "Test Connection Result", "Error: "+ex.getMessage());
				}
			}
		});

        return composite;
    }

    public Composite createButtons(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("  Cancel  ");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = CANCEL;
                shell.close();
            }
        });

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("  Save  ");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                store();
                action = SAVE;
                shell.close();
            }
        });

        return composite;
    }

	public void store() {
        String s = nameText.getText().trim();
        projectConfig.setName("".equals(s) ? null : s);

        s = typeCombo.getText().trim();
        projectConfig.setType(s);

        s = hostText.getText().trim();
        projectConfig.setHost("".equals(s) ? null : s);

        s = portText.getText().trim();
        projectConfig.setPort("".equals(s) ? 0 : Integer.parseInt(s));

        s = bindDnText.getText().trim();
        projectConfig.setUsername("".equals(s) ? null : s);

        s = bindPasswordText.getText().trim();
        projectConfig.setPassword("".equals(s) ? null : s);
	}

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setProjectConfig(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;

        nameText.setText(projectConfig.getName() == null ? "" : projectConfig.getName());
        typeCombo.setText(projectConfig.getType() == null ? PenroseClient.PENROSE : projectConfig.getType());
        hostText.setText(projectConfig.getHost() == null ? "localhost" : projectConfig.getHost());
        portText.setText(projectConfig.getPort() == 0 ? "" : ""+ projectConfig.getPort());
        bindDnText.setText(projectConfig.getUsername() == null ? "" : projectConfig.getUsername());
        bindPasswordText.setText(projectConfig.getPassword() == null ? "" : projectConfig.getPassword());
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
