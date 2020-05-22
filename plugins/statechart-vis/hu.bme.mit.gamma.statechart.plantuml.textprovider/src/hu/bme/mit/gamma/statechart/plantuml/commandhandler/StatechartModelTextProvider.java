/********************************************************************************
 * Copyright (c) 2018-2020 Contributors to the Gamma project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-1.0
 ********************************************************************************/
package hu.bme.mit.gamma.statechart.plantuml.commandhandler;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

import hu.bme.mit.gamma.statechart.plantuml.transformation.StatechartToPlantUMLTransformer;
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider2;
import net.sourceforge.plantuml.text.AbstractDiagramTextProvider;

public class StatechartModelTextProvider extends AbstractDiagramTextProvider implements DiagramTextProvider2 {

	private String plantumlModel;

	@Override
	public boolean supportsSelection(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			getPlantUMLCode(sel);
			if (plantumlModel != null) {
				return true;
			}
		}
		return false;
	}

	private void getPlantUMLCode(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			if (selection.size() == 1) {
				if (selection.getFirstElement() instanceof IFile) {
					IFile firstElement = (IFile) selection.getFirstElement();
					if (firstElement.getFileExtension().equals("gcd")) {
						getPlantUMLCode(firstElement.getFullPath());
					}
				}
			}
		}
	}

	private void getPlantUMLCode(IPath path) {
		ResourceSet resourceSet = new ResourceSetImpl();
		URI compositeSystemURI = URI.createPlatformResourceURI(path.toString(), true);
		Resource resource = resourceSet.getResource(compositeSystemURI, true);
		// Model processing
		StatechartToPlantUMLTransformer transformer = new StatechartToPlantUMLTransformer(resource);
		transformer.execute();
		if (transformer.getTransitions() != null) {
			plantumlModel = transformer.getTransitions();
		}
	}

	@Override
	public String getDiagramText(IPath path) {
		if (path.getFileExtension().equals("gcd")) {
			getPlantUMLCode(path);
		}
		return "@startuml\r\n" + plantumlModel + "@enduml";
	}

	@Override
	public String getDiagramText(IEditorPart editorPart, ISelection arg1, Map<String, Object> arg2) {
		IEditorInput input = editorPart.getEditorInput();
		IFile file = ResourceUtil.getFile(input);
		if (file != null) {
			IPath path = file.getFullPath();
			return getDiagramText(path);
		}
		return "";
	}

	@Override
	public boolean supportsPath(IPath arg0) {
		return "gcd".equals(arg0.getFileExtension());
	}

}
