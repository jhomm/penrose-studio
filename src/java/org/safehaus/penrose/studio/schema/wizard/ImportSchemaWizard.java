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
package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class ImportSchemaWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SchemaNameWizardPage namePage = new SchemaNameWizardPage();
    public SchemaFileWizardPage filePage = new SchemaFileWizardPage();

    public ImportSchemaWizard() {
        setWindowTitle("Import Schema");
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!filePage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            String schemaExtDir = "schema/ext";
            
            String name = namePage.getSchemaName();
            String path = schemaExtDir+"/"+name+".schema";

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ServerNode serverNode = objectsView.getSelectedProjectNode();
            if (serverNode == null) return false;

            String workDir = serverNode.getWorkDir();

            String file1 = filePage.getFilename();
            String file2 = workDir+File.separator+path;
            FileUtil.copy(file1, file2);

            SchemaConfig schemaConfig = new SchemaConfig();
            schemaConfig.setName(name);
            schemaConfig.setPath(path);

            Server server = serverNode.getProject();
            PenroseConfig penroseConfig = server.getPenroseConfig();
            penroseConfig.addSchemaConfig(schemaConfig);

            SchemaManager schemaManager = server.getSchemaManager();
            schemaManager.load(serverNode.getWorkDir(), schemaConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(namePage);
        addPage(filePage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
