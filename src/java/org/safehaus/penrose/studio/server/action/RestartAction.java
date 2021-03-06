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
package org.safehaus.penrose.studio.server.action;

import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class RestartAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public RestartAction() {
        setText("&Restart");
        setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.REFRESH));
        setToolTipText("Restart Penrose Server");
        setId(getClass().getName());
    }

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            ServerNode serverNode = serversView.getSelectedServerNode();
            Server server = serverNode.getServer();
            PenroseClient client = server.getClient();
            client.restart();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}
}