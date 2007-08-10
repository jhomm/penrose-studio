package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.nis.NISDomain;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    public Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    NISTool nisTool;
    NISDomain domain;

    public NISDomainNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;

        domain = (NISDomain)object;
        NISNode nisNode = (NISNode)parent;

        nisTool = nisNode.getNisTool();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Edit") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Create Partition") {
            public void run() {
                try {
                    createPartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Remove Partition") {
            public void run() {
                try {
                    removePartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Create Database") {
            public void run() {
                try {
                    createDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Load Database") {
            public void run() {
                try {
                    loadDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Clean Database") {
            public void run() {
                try {
                    cleanDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Remove Database") {
            public void run() {
                try {
                    removeDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
/*
        manager.add(new Action("Copy") {
            public void run() {
                try {
                    //copy(connection);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    //paste(connection);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISEditorInput ei = new NISEditorInput();
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());
    }

    public void edit() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        NISDomainDialog dialog = new NISDomainDialog(shell, SWT.NONE);
        dialog.setName(domain.getName());
        dialog.setPartition(domain.getPartition());
        dialog.setServer(domain.getServer());
        dialog.setSuffix(domain.getSuffix());
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        String domainName = dialog.getName();
        String partitionName = dialog.getPartition();
        String server = dialog.getServer();
        String suffix = dialog.getSuffix();

        NISDomain newDomain = new NISDomain();
        newDomain.setName(domainName);
        newDomain.setPartition(partitionName);
        newDomain.setServer(server);
        newDomain.setSuffix(suffix);

        nisTool.updateDomain(domain, newDomain);

        domain = newDomain;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        TreeViewer treeViewer = view.getTreeViewer();
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();

        boolean confirm = MessageDialog.openQuestion(shell,
                "Confirmation", "Remove selected domains?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.removePartition(domain);
            nisTool.removeDatabase(domain);
            nisTool.removePartitionConfig(domain);
            nisTool.removeDomain(domain);

            penroseStudio.removeDirectory("partitions/"+domain.getPartition());
        }

        penroseStudio.notifyChangeListeners();
    }

    public void createPartition() throws Exception {
        nisTool.createPartition(domain);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.uploadFolder("partitions/"+domain.getPartition());
        penroseStudio.notifyChangeListeners();
    }

    public void removePartition() throws Exception {
        nisTool.removePartition(domain);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.removeDirectory("partitions/"+domain.getPartition());
        penroseStudio.notifyChangeListeners();
    }

    public void createDatabase() throws Exception {
        nisTool.createDatabase(domain);
    }

    public void loadDatabase() throws Exception {
        nisTool.loadDatabase(domain);
    }

    public void cleanDatabase() throws Exception {
        nisTool.cleanDatabase(domain);
    }

    public void removeDatabase() throws Exception {
        nisTool.removeDatabase(domain);
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}