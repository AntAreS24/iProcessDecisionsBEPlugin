/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.tibco.bestudio.ui.importWizards;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.tibco.cep.be.iprocessdecisions.vocabulary.ParseVocabulary;
import com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Vocabulary;

public class ImportiProcessDecisionsVocabularyWizard extends Wizard implements IImportWizard {
	
	ImportiProcessDecisionsVocabularyWizardPage mainPage;

	public ImportiProcessDecisionsVocabularyWizard() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		ArrayList<Vocabulary> list = mainPage.getList();
		ParseVocabulary processor = new ParseVocabulary();
		
		IPath containerRoot = mainPage.getContainerFullPath();
		
		for (Vocabulary vocabulary : list) {
			processor.createConcept(containerRoot.makeAbsolute().toString(), vocabulary);
		}
		//FIXME do something with the list...
        return true;
	}
	 
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("iProcess Decisions - Vocabulary - Import Wizard"); //NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new ImportiProcessDecisionsVocabularyWizardPage("Import Vocabulary",selection); //NON-NLS-1
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages(); 
        addPage(mainPage);        
    }

}
