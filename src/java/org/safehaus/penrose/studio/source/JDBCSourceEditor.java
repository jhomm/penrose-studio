/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.source;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.source.JDBCSourceCachePage;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;
import org.apache.log4j.Logger;

import java.util.Iterator;

public class JDBCSourceEditor extends MultiPageEditorPart {

    Logger log = Logger.getLogger(getClass());

	SourceConfig sourceConfig;
    SourceConfig origSourceConfig;

    boolean dirty;

    Partition partition;
    JDBCSourcePropertyPage propertyPage;
    JDBCSourceCachePage cachePage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        JDBCSourceEditorInput ei = (JDBCSourceEditorInput)input;
        partition = ei.getPartition();
        origSourceConfig = ei.getSourceConfig();
        sourceConfig = (SourceConfig)origSourceConfig.clone();

        setSite(site);
        setInput(input);
        setPartName(partition.getName()+"/"+sourceConfig.getName());
    }

    protected void createPages() {
        try {
            propertyPage = new JDBCSourcePropertyPage(this);
            addPage(propertyPage.createControl());
            setPageText(0, "  Properties  ");

            cachePage = new JDBCSourceCachePage(this);
            addPage(cachePage.createControl());
            setPageText(1, "  Cache  ");

            load();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public Composite getParent() {
        return getContainer();
    }

    public void dispose() {
        propertyPage.dispose();
        cachePage.dispose();
        super.dispose();
    }

    public void load() throws Exception {
        propertyPage.load();
        cachePage.load();
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        if (!origSourceConfig.getName().equals(sourceConfig.getName())) {
            partition.renameSourceConfig(origSourceConfig, sourceConfig.getName());

            for (Iterator i=partition.getEntryMappings().iterator(); i.hasNext(); ) {
                EntryMapping entryMapping = (EntryMapping)i.next();
                for (Iterator j=entryMapping.getSourceMappings().iterator(); j.hasNext(); ) {
                    SourceMapping sourceMapping = (SourceMapping)j.next();
                    if (!sourceMapping.getSourceName().equals(origSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(sourceConfig.getName());
                }
            }
        }

        partition.modifySourceConfig(sourceConfig.getName(), sourceConfig);

        setPartName(partition.getName()+"/"+sourceConfig.getName());

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origSourceConfig.equals(sourceConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
