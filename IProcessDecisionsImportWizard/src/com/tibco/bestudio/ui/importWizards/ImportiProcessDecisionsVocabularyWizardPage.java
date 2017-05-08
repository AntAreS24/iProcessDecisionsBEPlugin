package com.tibco.bestudio.ui.importWizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.tibco.cep.be.iprocessdecisions.vocabulary.ParseVocabulary;
import com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Vocabulary;

@SuppressWarnings("restriction")
public class ImportiProcessDecisionsVocabularyWizardPage extends WizardDataTransferPage {
	protected ParseVocabulary processor;
	protected FileFieldEditor editor;
	protected Table tableVocabularyObjects;
	protected HashMap<String, Vocabulary> selectedVocabularyObjectList;
	protected IResource currentResourceSelection;

	private Button containerBrowseButton;
	private Text containerNameField;
	// initial value stores
	private String initialContainerFieldValue;

	// the validation message
	Label statusMessage;

	// for validating the selection
	ISelectionValidator validator;

	public ImportiProcessDecisionsVocabularyWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		setTitle(pageName); // NON-NLS-1
		setDescription("Import a file from the local file system into the workspace"); // NON-NLS-1
		selectedVocabularyObjectList = new HashMap<String, Vocabulary>();
		//Initialize to null
        currentResourceSelection = null;
        if (selection.size() == 1) {
            Object firstElement = selection.getFirstElement();
			currentResourceSelection = Platform.getAdapterManager().getAdapter(firstElement, IResource.class);
        }

        if (currentResourceSelection != null) {
            if (currentResourceSelection.getType() == IResource.FILE) {
				currentResourceSelection = currentResourceSelection.getParent();
			}

            if (!currentResourceSelection.isAccessible()) {
				currentResourceSelection = null;
			}
        }
	}

	
	@Override
	public void createControl(Composite parent) {
		Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);
		editor = new FileFieldEditor("fileSelect", "Select File: ", fileSelectionArea); // NON-NLS-1

		// Object Select
		Label objectSelectionLabel = new Label(fileSelectionArea, SWT.NONE);
		objectSelectionLabel.setText("Object(s) to import:");
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.horizontalSpan = 3;
		objectSelectionLabel.setLayoutData(data);

		tableVocabularyObjects = new Table(fileSelectionArea, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tableVocabularyObjects.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;

				System.out.println(item.getText() + " " + item.getChecked());
				if (item.getChecked()) {
					if (!selectedVocabularyObjectList.containsKey(item.getText())) {
						selectedVocabularyObjectList.put(item.getText(), new Vocabulary(item.getText()));
					}
				} else {
					if (selectedVocabularyObjectList.containsKey(item.getText())) {
						selectedVocabularyObjectList.remove(item.getText());
					}

				}
				checkIfPageComplete();
			}
		});
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 3;
		tableVocabularyObjects.setLayoutData(data);

		// container label
		Label resourcesLabel = new Label(fileSelectionArea, SWT.NONE);
		resourcesLabel.setText("Target project:");
		resourcesLabel.setFont(parent.getFont());

		// container name entry field
		containerNameField = new Text(fileSelectionArea, SWT.SINGLE | SWT.BORDER);
		BidiUtils.applyBidiProcessing(containerNameField, "file");

		containerNameField.addListener(SWT.Modify, this);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		containerNameField.setLayoutData(data);
		containerNameField.setFont(parent.getFont());

		// container browse button
		containerBrowseButton = new Button(fileSelectionArea, SWT.PUSH);
		containerBrowseButton.setText("Browse");
		containerBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		containerBrowseButton.addListener(SWT.Selection, this);
		containerBrowseButton.setFont(parent.getFont());
		setButtonLayoutData(containerBrowseButton);

		// //NON-NLS-2
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IPath path = new Path(ImportiProcessDecisionsVocabularyWizardPage.this.editor.getStringValue());
				String pathString = path.makeAbsolute().toString();
				// Not required
				// setDescription("File selected as: " + pathString);

				System.out.println("Starting to parse file");
				processor = new ParseVocabulary();
				ArrayList<Vocabulary> list = processor.getListOfVocabularyObjects(pathString, true);

				Collections.sort(list);

				for (Vocabulary vocabularyObject : list) {
					TableItem item = new TableItem(tableVocabularyObjects, SWT.NONE);
					item.setText(vocabularyObject.getName());
					item.setChecked(true);
				}

				for (Vocabulary vocabulary : list) {
					selectedVocabularyObjectList.put(vocabulary.getName(), vocabulary);
				}

				checkIfPageComplete();
			}
		});
		String[] extensions = new String[] { "*.cvj" }; // NON-NLS-1
		editor.setFileExtensions(extensions);
		fileSelectionArea.moveAbove(null);

		setControl(fileSelectionArea);
		checkIfPageComplete();
		initialPopulateContainerField();
	}

	protected void checkIfPageComplete() {
		boolean result = false;
		if (selectedVocabularyObjectList != null && selectedVocabularyObjectList.size() > 0) {
			result = true;
		}
		if (currentResourceSelection != null) {
            if (currentResourceSelection.isAccessible()) {
            	result = result | true;
			}
		}else{
			result = false;
		}
		setPageComplete(result);
	}

	public ArrayList<Vocabulary> getList() {
		return new ArrayList<>(selectedVocabularyObjectList.values());
	}

	/**
     * Sets the initial contents of the container name field.
     */
    protected final void initialPopulateContainerField() {
        if (initialContainerFieldValue != null) {
			containerNameField.setText(initialContainerFieldValue);
		} else if (currentResourceSelection != null) {
			containerNameField.setText(currentResourceSelection.getFullPath()
                    .makeRelative().toString());
		}
        checkIfPageComplete();
    }
    
	@Override
	public void handleEvent(Event event) {
		Widget source = event.widget;

		if (source == containerBrowseButton) {
			handleContainerBrowseButtonPressed();
		}

		updateWidgetEnablements();
	}

	/**
	 * Opens a container selection dialog and displays the user's subsequent
	 * container resource selection in this page's container name field.
	 */
	protected void handleContainerBrowseButtonPressed() {
		// see if the user wishes to modify this container selection
		IPath containerPath = queryForContainer(getSpecifiedContainer(), "Select a folder to import into.",
				"Import into Folder");

		// if a container was selected then put its name in the container name
		// field
		if (containerPath != null) { // null means user cancelled
			setErrorMessage(null);
			if(containerPath.segmentCount() > 1){
				containerPath = containerPath.removeLastSegments(containerPath.segmentCount()-1);
			}
			
			containerNameField.setText(containerPath.makeRelative().toString());
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			currentResourceSelection = workspaceRoot.findMember(containerPath);
		}
		checkIfPageComplete();
	}

	/**
	 * Returns the container resource specified in the container name entry
	 * field, or <code>null</code> if such a container does not exist in the
	 * workbench.
	 *
	 * @return the container resource specified in the container name entry
	 *         field, or <code>null</code>
	 */
	protected IContainer getSpecifiedContainer() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		IPath path = getContainerFullPath();
		if (workspace.getRoot().exists(path)) {
			IResource resource = workspace.getRoot().findMember(path);
			if (resource.getType() == IResource.FILE) {
				return null;
			}
			return (IContainer) resource;

		}

		return null;
	}

	/**
	 * Returns the path of the container resource specified in the container
	 * name entry field, or <code>null</code> if no name has been typed in.
	 * <p>
	 * The container specified by the full path might not exist and would need
	 * to be created.
	 * </p>
	 *
	 * @return the full path of the container resource specified in the
	 *         container name entry field, or <code>null</code>
	 */
	protected IPath getContainerFullPath() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		// make the path absolute to allow for optional leading slash
		IPath testPath = getResourcePath();

		if (testPath.equals(workspace.getRoot().getFullPath())) {
			return testPath;
		}

		IStatus result = workspace.validatePath(testPath.toString(),
				IResource.PROJECT | IResource.FOLDER | IResource.ROOT);
		if (result.isOK()) {
			return testPath;
		}

		return null;
	}

	/**
	 * Return the path for the resource field.
	 * 
	 * @return IPath
	 */
	protected IPath getResourcePath() {
		if (this.containerNameField != null) {
			return getPathFromText(this.containerNameField);
		}

		if (this.initialContainerFieldValue != null && this.initialContainerFieldValue.length() > 0) {
			return new Path(this.initialContainerFieldValue).makeAbsolute();
		}

		return Path.EMPTY;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}
}
