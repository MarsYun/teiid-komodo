/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.relational.commands.server;

import java.util.Arrays;
import java.util.List;
import org.komodo.relational.commands.RelationalShellCommand;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.utils.i18n.I18n;

/**
 * A base class for @{link {@link Teiid server}-related shell commands.
 */
abstract class ServerShellCommand extends RelationalShellCommand {

    protected static final String ADMIN_PORT = "adminPort"; //$NON-NLS-1$
    protected static final String ADMIN_PSWD = "adminPswd"; //$NON-NLS-1$
    protected static final String ADMIN_USER = "adminUser"; //$NON-NLS-1$
    protected static final String ADMIN_SECURE = "adminSecure"; //$NON-NLS-1$
    protected static final String JDBC_PORT = "jdbcPort"; //$NON-NLS-1$
    protected static final String JDBC_PSWD = "jdbcPswd"; //$NON-NLS-1$
    protected static final String JDBC_USER = "jdbcUser"; //$NON-NLS-1$
    protected static final String JDBC_SECURE = "jdbcSecure"; //$NON-NLS-1$
    protected static final String HOST = "host"; //$NON-NLS-1$

    protected static final List< String > ALL_PROPS = Arrays.asList( new String[] { ADMIN_PORT, ADMIN_PSWD, ADMIN_USER, ADMIN_SECURE,
                                                                                    JDBC_PORT, JDBC_PSWD, JDBC_USER, JDBC_SECURE, HOST } );
    protected ServerShellCommand( final String name,
                                  final WorkspaceStatus status ) {
        super( status, name );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.api.ShellCommand#isValidForCurrentContext()
     */
    @Override
    public boolean isValidForCurrentContext() {
        return true;
    }

    protected boolean isWorkspaceContext() {
        return getWorkspaceStatus().getLabelProvider().isWorkspacePath(getContext().getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.commands.RelationalShellCommand#getCategory()
     */
    @Override
    public String getCategory() {
        return I18n.bind( ServerCommandsI18n.commandCategory );
    }

}
