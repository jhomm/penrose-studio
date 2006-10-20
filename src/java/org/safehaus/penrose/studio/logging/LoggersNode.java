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
package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.LoggerConfig;
import org.safehaus.penrose.log4j.RootConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class LoggersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    ProjectNode projectNode;

    public LoggersNode(
            ObjectsView view,
            ProjectNode projectNode,
            String name,
            String type,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, type, image, object, parent);
        this.view = view;
        this.projectNode = projectNode;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Root Logger") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("New Logger...") {
            public void run() {
                try {
                    createLogger();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        Project project = projectNode.getProject();
        Log4jConfig loggingConfig = project.getLog4jConfig();

        RootConfig rootConfig = loggingConfig.getRootConfig();
        if (rootConfig == null) rootConfig = new RootConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.setRootConfig(rootConfig);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        if (rootConfig.getLevel() == null && rootConfig.getAppenders().isEmpty()) {
            loggingConfig.setRootConfig(null);

        } else {
            if (loggingConfig.getRootConfig() == null) loggingConfig.setRootConfig(rootConfig);
        }
    }

    public void createLogger() throws Exception {

        Project project = projectNode.getProject();
        Log4jConfig loggingConfig = project.getLog4jConfig();

        LoggerConfig loggerConfig = new LoggerConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Add Logger");
        dialog.setLoggerConfig(loggerConfig);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        loggingConfig.addLoggerConfig(loggerConfig);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public boolean hasChildren() throws Exception {
        Project project = projectNode.getProject();
        Log4jConfig loggingConfig = project.getLog4jConfig();
        return !loggingConfig.getLoggerConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Project project = projectNode.getProject();
        Log4jConfig loggingConfig = project.getLog4jConfig();

        for (Iterator i=loggingConfig.getLoggerConfigs().iterator(); i.hasNext(); ) {
            LoggerConfig loggerConfig = (LoggerConfig)i.next();

            LoggerNode loggerNode = new LoggerNode(
                    view,
                    projectNode,
                    loggerConfig.getName(),
                    ObjectsView.LOGGER,
                    PenrosePlugin.getImage(PenroseImage.LOGGER),
                    loggerConfig,
                    this
            );

            children.add(loggerNode);
        }

        return children;
    }
}
