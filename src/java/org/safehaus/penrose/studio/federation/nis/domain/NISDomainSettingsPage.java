package org.safehaus.penrose.studio.federation.nis.domain;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;

/**
 * @author Endi S. Dewata
 */
public class NISDomainSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISDomainEditor editor;
    NISFederation nisFederation;
    NISDomain domain;

    Project project;

    public NISDomainSettingsPage(NISDomainEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.nisFederation = editor.getNisFederation();
        this.domain = editor.getDomain();

        this.project = nisFederation.getProject();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section domainSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        domainSection.setText("NIS Domain");
        domainSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control domainControl = createDomainsSection(domainSection);
        domainSection.setClient(domainControl);

        new Label(body, SWT.NONE);

        Section nisSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        nisSection.setText("NIS Partition");
        nisSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nisControl = createNisControl(nisSection);
        nisSection.setClient(nisControl);

        new Label(body, SWT.NONE);

        Section nssSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        nssSection.setText("NSS Partition");
        nssSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nssControl = createNssPanel(nssSection);
        nssSection.setClient(nssControl);
    }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        domainLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        Label domainText = toolkit.createLabel(composite, domain.getFullName());
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label serverLabel = toolkit.createLabel(composite, "Server:");
        serverLabel.setLayoutData(new GridData());

        Label serverText = toolkit.createLabel(composite, domain.getServer());
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNisControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createNisLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createNisRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createNisLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label suffixLabel = toolkit.createLabel(composite, "Suffix:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        suffixLabel.setLayoutData(gd);

        String suffix = domain.getSuffix();
        if (suffix == null) suffix = "";

        Label suffixText = toolkit.createLabel(composite, suffix);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNisRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.createYpPartitionConfig(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    PenroseClient penroseClient = project.getClient();
                    PartitionConfig nisPartitionConfig = nisFederation.getPartitionConfig(domain.getName()+"_"+NISFederation.YP);
                    penroseClient.stopPartition(nisPartitionConfig.getName());
                    nisFederation.removePartitionConfig(nisPartitionConfig.getName());
                    project.removeDirectory("partitions/"+nisPartitionConfig.getName());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createNssPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createNssLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createNssRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createNssLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label nssSuffixLabel = toolkit.createLabel(composite, "Suffix:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        nssSuffixLabel.setLayoutData(gd);

        String nssSuffix = domain.getNssSuffix();
        if (nssSuffix == null) nssSuffix = "";

        Label nssSuffixText = toolkit.createLabel(composite, nssSuffix);
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNssRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.createNssPartitionConfig(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    PenroseClient penroseClient = project.getClient();
                    PartitionConfig nssPartitionConfig = nisFederation.getPartitionConfig(domain.getName()+"_"+NISFederation.NSS);
                    penroseClient.stopPartition(nssPartitionConfig.getName());
                    nisFederation.removePartitionConfig(nssPartitionConfig.getName());
                    project.removeDirectory("partitions/"+nssPartitionConfig.getName());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }
}
