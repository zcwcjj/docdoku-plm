/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is Document of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A DocumentICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.interceptor;

import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.services.IAccessRightLocal;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@ChangeItemAccess
public class ChangeItemAccessInterceptor {
    @EJB
    private IAccessRightLocal accessRight;

    @AroundInvoke
    public Object checkChangeItemAccess(InvocationContext ctx) throws Exception {
        if(ctx.getParameters() != null && ctx.getParameters().length > 0 ){
            ChangeItem changeItem;
            ChangeItemAccess.ParseType parseType = ctx.getMethod().getAnnotation(ChangeItemAccess.class).parseType();
            switch (parseType){
                case NONE:
                    if(ctx.getParameters()[0] instanceof ChangeItem){
                        changeItem = ((ChangeItem) ctx.getParameters()[0]);
                    }else{
                        throw new NotAllowedException("NotAllowedException42");
                    }
                    break;
                default:
                    throw new NotAllowedException("NotAllowedException42");
            }
            ChangeItemAccess.Right right = ctx.getMethod().getAnnotation(ChangeItemAccess.class).right();
            switch (right){
                case READ: accessRight.checkChangeItemReadAccess(changeItem);break;
                case WRITE: accessRight.checkChangeItemWriteAccess(changeItem);break;
                case GRANT: accessRight.checkChangeItemGrantAccess(changeItem);break;
                default:throw new NotAllowedException("NotAllowedException42");
            }
            return ctx.proceed();
        }else{
            throw new NotAllowedException("NotAllowedException42");
        }
    }
}