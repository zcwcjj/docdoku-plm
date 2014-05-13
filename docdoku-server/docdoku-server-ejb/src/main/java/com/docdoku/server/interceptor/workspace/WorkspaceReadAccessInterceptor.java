/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.interceptor.workspace;

import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.interceptor.WorkspaceAccess;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


@WorkspaceAccess(right=WorkspaceAccess.Right.READ)
public class WorkspaceReadAccessInterceptor {
    @EJB
    private IUserManagerLocal userManager;

    @AroundInvoke
    public Object checkWorkspaceReadAccessWithId(InvocationContext ctx) throws Exception {
        if(ctx.getParameters() != null && ctx.getParameters().length > 0 && ctx.getParameters()[0] instanceof String){
            userManager.checkWorkspaceReadAccess((String) ctx.getParameters()[0]);
        }else{

        }

        return ctx.proceed();
    }


}
