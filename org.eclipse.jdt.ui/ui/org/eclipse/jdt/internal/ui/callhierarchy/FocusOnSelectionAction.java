/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 * 			(report 36180: Callers/Callees view)
 ******************************************************************************/
package org.eclipse.jdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

import org.eclipse.jdt.internal.ui.util.SelectionUtil;

import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

class FocusOnSelectionAction extends Action {
    private CallHierarchyViewPart fPart;

    public FocusOnSelectionAction(CallHierarchyViewPart part) {
        super("&Focus On Selection");
        fPart= part;
        setDescription("Focus On Selection");
        setToolTipText("Focus On Selection");
    }

    public boolean canActionBeAdded() {
        Object element = SelectionUtil.getSingleElement(getSelection());

        IMethod method = null;
        
        if (element instanceof IMethod) {
            method= (IMethod) element;
        } else if (element instanceof IAdaptable) {
            method= (IMethod) ((IAdaptable) element).getAdapter(IMethod.class);
        }
        
        if (method != null) {
            setText("Focus On '" + method.getElementName() + "'");

            return true;
        }

        return false;
    }

    /*
     * @see Action#run
     */
    public void run() {
        Object element = SelectionUtil.getSingleElement(getSelection());

        if (element instanceof MethodWrapper) {
            IMember member= ((MethodWrapper) element).getMember();
            if (member.getElementType() == IJavaElement.METHOD) {
                fPart.setMethod((IMethod) member);
            }
        }
    }

    private ISelection getSelection() {
        ISelectionProvider provider = fPart.getSite().getSelectionProvider();

        if (provider != null) {
            return provider.getSelection();
        }

        return null;
    }
}