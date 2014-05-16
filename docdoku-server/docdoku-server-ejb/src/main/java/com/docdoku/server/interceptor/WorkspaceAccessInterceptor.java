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
package com.docdoku.server.interceptor;

import com.docdoku.core.common.EntityKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.services.IAccessRightLocal;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@WorkspaceAccess
public class WorkspaceAccessInterceptor {
    @EJB
    private IAccessRightLocal accessRight;

    @AroundInvoke
    public Object checkWorkspaceAccess(InvocationContext ctx) throws Exception {
        if(ctx.getParameters() != null && ctx.getParameters().length > 0 ){
            String workspaceId;
            WorkspaceAccess.ParseType parseType = ctx.getMethod().getAnnotation(WorkspaceAccess.class).parseType();
            switch (parseType){
                case NONE:
                    if(ctx.getParameters()[0] instanceof String){
                        workspaceId =(String) ctx.getParameters()[0];
                    }else if(ctx.getParameters()[0] instanceof EntityKey){
                        workspaceId = ((EntityKey) ctx.getParameters()[0]).getWorkspaceId();
                    }else{
                        throw new NotAllowedException("NotAllowedException42");
                    }
                    break;
                case FOLDER:
                    if(ctx.getParameters()[0] instanceof String){
                        workspaceId = Folder.parseWorkspaceId((String) ctx.getParameters()[0]);break;
                    }else{
                        throw new NotAllowedException("NotAllowedException42");
                    }
                default:
                    throw new NotAllowedException("NotAllowedException42");
            }

            WorkspaceAccess.Right right = ctx.getMethod().getAnnotation(WorkspaceAccess.class).right();
            switch (right){
                case READ: accessRight.checkWorkspaceReadAccess(workspaceId);break;
                case WRITE: accessRight.checkWorkspaceWriteAccess(workspaceId);break;
                case ADMIN: accessRight.checkWorkspaceAdminAccess(workspaceId);break;
                default:throw new NotAllowedException("NotAllowedException42");
            }
            return ctx.proceed();
        }else{
            throw new NotAllowedException("NotAllowedException42");
        }
    }
}