/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.reorg;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.testplugin.JavaProjectHelper;

import org.eclipse.jdt.ui.tests.refactoring.MySetup;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;

import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ICopyQueries;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg2.CopyRefactoring2;
import org.eclipse.jdt.internal.corext.refactoring.reorg2.ReorgUtils2;


public class CopyTest extends RefactoringTest {

	private static final Class clazz= CopyTest.class;
	private static final String REFACTORING_PATH= "Copy/";

	public CopyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new MySetup(new TestSuite(clazz));
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	private void verifyDisabled(IResource[] resources, IJavaElement[] javaElements) throws JavaModelException {
		assertTrue("delete should be disabled", ! CopyRefactoring2.isAvailable(resources, javaElements));
		CopyRefactoring2 refactoring2= CopyRefactoring2.create(resources, javaElements);
		assertTrue(refactoring2 == null);
	}

	private CopyRefactoring2 verifyEnabled(IResource[] resources, IJavaElement[] javaElements, ICopyQueries copyQueries) throws JavaModelException {
		assertTrue("delete should be enabled", CopyRefactoring2.isAvailable(resources, javaElements));
		CopyRefactoring2 refactoring2= CopyRefactoring2.create(resources, javaElements);
		assertNotNull(refactoring2);
		if (copyQueries != null)
			refactoring2.setQueries(copyQueries);
		return refactoring2;
	}

	private void verifyInvalidDestination(CopyRefactoring2 ref, Object destination) throws Exception {
		RefactoringStatus status= null;
		if (destination instanceof IResource)
			status= ref.setDestination((IResource)destination);
		else if (destination instanceof IJavaElement)
			status= ref.setDestination((IJavaElement)destination);
		else assertTrue(false);
		
		assertEquals("destination was expected to be not valid",  RefactoringStatus.FATAL, status.getSeverity());
	}
	
	private void verifyValidDestination(CopyRefactoring2 ref, Object destination) throws Exception {
		RefactoringStatus status= null;
		if (destination instanceof IResource)
			status= ref.setDestination((IResource)destination);
		else if (destination instanceof IJavaElement)
			status= ref.setDestination((IJavaElement)destination);
		else assertTrue(false);
		
		assertEquals("destination was expected to be valid: " + status.getFirstMessage(status.getSeverity()), RefactoringStatus.OK, status.getSeverity());
	}

	private static class TestCopyQueries implements ICopyQueries{

		private static final String NEW_PACKAGE_NAME= "unused.name";
		private static final String NEW_FILE_NAME= "UnusedName.gif";
		private static final String NEW_FOLDER_NAME= "UnusedName";
		private static final String NEW_CU_NAME= "UnusedName";
		
		public INewNameQuery createNewCompilationUnitNameQuery(ICompilationUnit cu) {
			return createStaticQuery(NEW_CU_NAME);
		}

		public INewNameQuery createNewResourceNameQuery(IResource res) {
			if (res instanceof IFile)
				return createStaticQuery(NEW_FILE_NAME);
			else
				return createStaticQuery(NEW_FOLDER_NAME);
		}

		public INewNameQuery createNewPackageNameQuery(IPackageFragment pack) {
			return createStaticQuery(NEW_PACKAGE_NAME);
		}

		public INewNameQuery createNullQuery() {
			return createStaticQuery(null);
		}

		public INewNameQuery createStaticQuery(final String newName) {
			return new INewNameQuery(){
				public String getNewName() {
					return newName;
				}
			};
		}
	}
	//---------------
	public void testDisabled_empty() throws Exception {
		IJavaElement[] javaElements= {};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);
	}
	
	public void testDisabled_null_element() throws Exception {
		IJavaElement[] javaElements= {null};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);
	}

	public void testDisabled_null_resource() throws Exception {
		IJavaElement[] javaElements= {};
		IResource[] resources= {null};
		verifyDisabled(resources, javaElements);
	}

	public void testDisabled_javaProject() throws Exception {
		IJavaElement[] javaElements= {MySetup.getProject()};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);
	}

	public void testDisabled_defaultPackage() throws Exception {
		IPackageFragment defaultPackage= getRoot().getPackageFragment("");
		assertTrue(defaultPackage.exists());
		IJavaElement[] javaElements= {defaultPackage};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);
	}

	public void testDisabled_project() throws Exception {
		IJavaElement[] javaElements= {};
		IResource[] resources= {MySetup.getProject().getProject()};
		verifyDisabled(resources, javaElements);
	}

	public void testDisabled_notExistingElement() throws Exception {
		ICompilationUnit notExistingCu= getPackageP().getCompilationUnit("NotMe.java");
		assertTrue(! notExistingCu.exists());
		IJavaElement[] javaElements= {notExistingCu};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);		
	}

	public void testDisabled_notExistingResource() throws Exception {
		IFolder folder= (IFolder)getPackageP().getResource();
		IFile notExistingFile= folder.getFile("a.txt");
		
		IJavaElement[] javaElements= {};
		IResource[] resources= {notExistingFile};
		verifyDisabled(resources, javaElements);
	}
	
	public void testDisabled_noCommonParent0() throws Exception {
		IJavaElement[] javaElements= {getPackageP(), getRoot()};
		IResource[] resources= {};
		verifyDisabled(resources, javaElements);		
	}
	
	public void testDisabled_noCommonParent1() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}}", false, new NullProgressMonitor());
		try {
			IType classA= cu.getType("A");
			IMethod	methodFoo= classA.getMethod("foo", new String[0]);
			IJavaElement[] javaElements= { classA, methodFoo };
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}

	public void testDisabled_noCommonParent2() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}}", false, new NullProgressMonitor());
		try {
			IType classA= cu.getType("A");
			IJavaElement[] javaElements= { classA, cu};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}

	public void testDisabled_noCommonParent3() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}}", false, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= {cu, getPackageP()};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}

	public void testDisabled_noCommonParent5() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}}", false, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= {cu, getRoot()};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}

	public void testDisabled_noCommonParent6() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}}", false, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= {cu, getRoot()};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}

	public void testDisabled_noCommonParent7() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{class Inner{}}", false, new NullProgressMonitor());
		try {
			IType classA= cu.getType("A");
			IType classInner= classA.getType("Inner");
			IJavaElement[] javaElements= { classA, classInner};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}
	}
	
	public void testDisabled_noCommonParent8() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try {
			IType classA= cu.getType("A");
			IType classInner= classA.getType("Inner");
			IMethod	methodFoo= classA.getMethod("foo", new String[0]);
			IJavaElement[] javaElements= { methodFoo, classInner};
			IResource[] resources= {};
			verifyDisabled(resources, javaElements);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}
	}

	public void testEnabled_cu() throws Exception {
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= { cu};
			IResource[] resources= {};
			verifyEnabled(resources, javaElements, null);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}		
	}
	
	public void testEnabled_package() throws Exception {
		IJavaElement[] javaElements= { getPackageP()};
		IResource[] resources= {};
		verifyEnabled(resources, javaElements, null);
	}	
	
	public void testEnabled_packageRoot() throws Exception {
		IJavaElement[] javaElements= { getRoot()};
		IResource[] resources= {};
		verifyEnabled(resources, javaElements, null);
	}	

	public void testEnabled_file() throws Exception {
		IFolder folder= (IFolder)getPackageP().getResource();
		IFile file= folder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			verifyEnabled(resources, javaElements, null);			
		} finally{
			performDummySearch();
			file.delete(true, false, null);
		}
	}	

	public void testEnabled_folder() throws Exception{
		IFolder folder= (IFolder)getPackageP().getResource();
		
		IJavaElement[] javaElements= {};
		IResource[] resources= {folder};
		verifyEnabled(resources, javaElements, null);			
	}

	public void testEnabled_fileFolder() throws Exception {
		IFolder superFolder= (IFolder)getPackageP().getResource();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file, folder};
			verifyEnabled(resources, javaElements, null);			
		} finally{
			performDummySearch();
			file.delete(true, false, null);
			folder.delete(true, false, null);
		}
	}	

	public void testEnabled_fileFolderCu() throws Exception {
		IFolder superFolder= (IFolder)getPackageP().getResource();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file, folder};
			verifyEnabled(resources, javaElements, null);			
		} finally{
			performDummySearch();
			file.delete(true, false, null);
			folder.delete(true, false, null);
			cu.delete(true, new NullProgressMonitor());
		}
	}
	
	public void testDestination_package_no_1() throws Exception{
		IJavaElement[] javaElements= { getPackageP()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);
		verifyInvalidDestination(ref, MySetup.getProject());
	}

	public void testDestination_package_no_2() throws Exception{		
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		try {
			IJavaElement[] javaElements= { getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination=cu;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_package_no_3() throws Exception{		
		IFolder superFolder= (IFolder)getPackageP().getResource();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);

		try {
			IJavaElement[] javaElements= { getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= file;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			file.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_package_no_4() throws Exception{		
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		try {
			IJavaElement[] javaElements= { getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= folder;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_cu_no_0() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		ICompilationUnit cu2= getPackageP().createCompilationUnit("B.java", "package p;class B{}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			IType classB= cu2.getType("B");
			Object destination= classB;
			verifyInvalidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			cu2.delete(true, new NullProgressMonitor());
		}
	}
	
	public void testDestination_cu_no_1() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		assertTrue(! simpleProject.isOpen());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= simpleProject;
			verifyInvalidDestination(ref, destination);
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			simpleProject.delete(true, true, null);
		}
	}

	public void testDestination_file_no_0() throws Exception{
		IFolder superFolder= (IFolder)getPackageP().getResource();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);

		ICompilationUnit cu2= getPackageP().createCompilationUnit("B.java", "package p;class B{}", false, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			IType classB= cu2.getType("B");			
			Object destination= classB;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			file.delete(true, new NullProgressMonitor());			
			cu2.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_folder_no_0() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= folder;//same folder
			verifyInvalidDestination(ref, destination);	
		}finally{
			performDummySearch();
			folder.delete(true, false, null);
		}
	}

	public void testDestination_folder_no_1() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IFolder childFolder= folder.getFolder("folder");
		childFolder.create(true, true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= childFolder;
			verifyInvalidDestination(ref, destination);			
		}finally{
			performDummySearch();
			folder.delete(true, false, null);
			childFolder.delete(true, false, null);
		}
	}
	
	public void testDestination_folder_no_2() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IFile childFile= folder.getFile("a.txt");
		childFile.create(getStream("123"), true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= childFile;
			verifyInvalidDestination(ref, destination);			
		}finally{
			performDummySearch();
			folder.delete(true, false, null);
			childFile.delete(true, false, null);
		}
	}
	
	public void testDestination_folder_no_3() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		assertTrue(! simpleProject.isOpen());
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= simpleProject;
			verifyInvalidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			simpleProject.delete(true, true, new NullProgressMonitor());
		}
	}

	
	public void testDestination_root_no_0() throws Exception{
		IJavaElement[] javaElements= {getRoot()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

		Object destination= getPackageP();
		verifyInvalidDestination(ref, destination);			
	}

	public void testDestination_root_no_1() throws Exception{
		ICompilationUnit cu= getPackageP().createCompilationUnit("B.java", "package p;class B{}", false, new NullProgressMonitor());
		
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= cu;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}			
	}

	public void testDestination_root_no_2() throws Exception{
		ICompilationUnit cu= getPackageP().createCompilationUnit("B.java", "package p;class B{}", false, new NullProgressMonitor());
		
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			IType classB= cu.getType("B");
			Object destination= classB;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}			
	}
	
	public void testDestination_root_no_3() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);

		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= file;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}			
	}

	public void testDestination_root_no_4() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= folder;
			verifyInvalidDestination(ref, destination);
		} finally {
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_root_no_5() throws Exception{
		IJavaElement[] javaElements= { getRoot()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

		Object destination= getRoot();
		verifyInvalidDestination(ref, destination);
	}
	
	public void testDestination_cu_yes_0() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);
			Object destination= cu1;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_cu_yes_1() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getPackageP();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_cu_yes_2() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		IPackageFragment otherPackage= getRoot().createPackageFragment("otherPackage", true, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= otherPackage;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			otherPackage.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_cu_yes_3() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_cu_yes_4() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_cu_yes_5() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		simpleProject.open(null);
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= simpleProject;
			verifyValidDestination(ref, destination);
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			simpleProject.delete(true, true, null);
		}
	}

	public void testDestination_cu_yes_6() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= file;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			file.delete(true, false, null);
		}
	}

	public void testDestination_cu_yes_7() throws Exception{
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);
		try{
			IJavaElement[] javaElements= { cu1};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= folder;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			cu1.delete(true, new NullProgressMonitor());
			folder.delete(true, false, null);
		}
	}

	public void testDestination_file_yes_0() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= file;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}
	}		

	public void testDestination_file_yes_1() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		IFile otherFile= superFolder.getFile("b.txt");
		otherFile.create(getStream("123"), true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= otherFile;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
			otherFile.delete(true, new NullProgressMonitor());
		}
	}		

	public void testDestination_file_yes_3() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= folder;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
			folder.delete(true, new NullProgressMonitor());
		}
	}		

	public void testDestination_file_yes_4() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		ICompilationUnit cu1= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());			
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= cu1;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
			cu1.delete(true, new NullProgressMonitor());
		}
	}		
	
	public void testDestination_file_yes_5() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getPackageP();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_file_yes_6() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}
	}		

	public void testDestination_file_yes_7() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFile file= superFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}
	}		

	public void testDestination_file_yes_8() throws Exception{
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		IFile file= parentFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {file};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= parentFolder;
			verifyValidDestination(ref, destination);			
		}finally{
			performDummySearch();
			file.delete(true, new NullProgressMonitor());
		}
	}
			
	public void testDestination_folder_yes_0() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IFolder otherFolder= superFolder.getFolder("otherfolder");
		otherFolder.create(true, true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= otherFolder;
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			otherFolder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_folder_yes_1() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_folder_yes_2() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testDestination_folder_yes_3() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= getPackageP();
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}
	
	public void testDestination_folder_yes_4() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IFolder otherFolder= superFolder.getFolder("otherfolder");
		otherFolder.create(true, true, null);
		IFile fileInAnotherFolder= otherFolder.getFile("f.tex");
		fileInAnotherFolder.create(getStream("123"), true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= fileInAnotherFolder;
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());	
			otherFolder.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_folder_yes_5() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= cu;
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			cu.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_folder_yes_6() throws Exception{
		IProject superFolder= MySetup.getProject().getProject();
		IFolder folder= superFolder.getFolder("folder");
		folder.create(true, true, null);

		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		simpleProject.open(null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= simpleProject;
			verifyValidDestination(ref, destination);						
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			simpleProject.delete(true, true, new NullProgressMonitor());
		}
	}

	public void testDestination_package_yes_0() throws Exception{
		IJavaElement[] javaElements= {getPackageP()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

		Object destination= getRoot();
		verifyValidDestination(ref, destination);						
	}

	public void testDestination_package_yes_1() throws Exception{
		IJavaElement[] javaElements= { getPackageP()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);
		verifyValidDestination(ref, getPackageP());
	}

	public void testDestination_package_yes_2() throws Exception{		
		IPackageFragment otherPackage= getRoot().createPackageFragment("other.pack", true, new NullProgressMonitor());
		try {
			IJavaElement[] javaElements= { getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= otherPackage;
			verifyValidDestination(ref, destination);
		} finally {
			performDummySearch();
			otherPackage.delete(true, new NullProgressMonitor());
		}
	}

	public void testDestination_root_yes_0() throws Exception{
		IJavaElement[] javaElements= {getRoot()};
		IResource[] resources= {};
		CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

		Object destination= getRoot().getJavaProject();
		verifyValidDestination(ref, destination);
	}
	
	public void testDestination_root_yes_1() throws Exception{
		IJavaProject otherJavaProject= JavaProjectHelper.createJavaProject("other", "bin");
		
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, null);

			Object destination= otherJavaProject;
			verifyValidDestination(ref, destination);
		} finally {
			performDummySearch();
			JavaProjectHelper.delete(otherJavaProject);
		}						
	}

	public void testCopy_File_to_Folder() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
		
		IProject superFolder= MySetup.getProject().getProject();
		IFolder destinationFolder= superFolder.getFolder("folder");
		destinationFolder.create(true, true, null);
		
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= destinationFolder;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= destinationFolder.getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			destinationFolder.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_Same_Folder() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
		
		IFolder destinationFolder= parentFolder;
		
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= destinationFolder;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= destinationFolder.getFile(TestCopyQueries.NEW_FILE_NAME);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}
	
	public void testCopy_File_to_Itself() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
				
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= file;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= parentFolder.getFile(TestCopyQueries.NEW_FILE_NAME);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_AnotherFile() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);

		IFile otherFile= MySetup.getProject().getProject().getFile("b.txt");
		otherFile.create(getStream("123"), true, null);
				
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= otherFile;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= parentFolder.getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_Package() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
		
		IPackageFragment otherPackage= getRoot().createPackageFragment("other.pack", true, new NullProgressMonitor());
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= otherPackage;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= ((IFolder)otherPackage.getResource()).getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			otherPackage.delete(true, new NullProgressMonitor());
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_DefaultPackage() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
		
		IPackageFragment defaultPackage= getRoot().getPackageFragment("");
		assertTrue(defaultPackage.exists());
		assertTrue(defaultPackage.isDefaultPackage());
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= defaultPackage;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= ((IFolder)defaultPackage.getResource()).getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_SourceFolder() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
				
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= ((IFolder)getRoot().getResource()).getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_JavaProject() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
				
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= MySetup.getProject().getProject().getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_File_to_Cu() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
				
		IFile newFile= null;
		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= cu;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= parentFolder.getFile(TestCopyQueries.NEW_FILE_NAME);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			cu.delete(true, new NullProgressMonitor());
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}
	
	public void testCopy_File_to_SimpleProject() throws Exception {
		IFolder parentFolder= (IFolder) getPackageP().getResource();
		String fileName= "a.txt";
		IFile file= parentFolder.getFile(fileName);
		file.create(getStream("123"), true, null);
				
		IFile newFile= null;
		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		simpleProject.open(null);
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {};
			IResource[] resources= { file };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= simpleProject;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", file.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", file.exists());
			
			newFile= simpleProject.getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			file.delete(true, false, null);
			simpleProject.delete(true, new NullProgressMonitor());
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_Cu_to_Folder() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IProject superFolder= MySetup.getProject().getProject();
		IFolder destinationFolder= superFolder.getFolder("folder");
		destinationFolder.create(true, true, null);
		
		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= { };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= destinationFolder;
			verifyValidDestination(ref, destination);
			
			assertTrue("source cu does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source cu does not exist after copying", cu.exists());
			
			newFile= destinationFolder.getFile(fileName);
			assertTrue("new cu does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			destinationFolder.delete(true, false, null);
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_Cu_to_Same_Package() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		ICompilationUnit newCu= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= { };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);
			
			Object destination= getPackageP();
			verifyValidDestination(ref, destination);
			
			assertTrue("source cu does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source cu does not exist after copying", cu.exists());
			
			newCu= getPackageP().getCompilationUnit(TestCopyQueries.NEW_CU_NAME + ".java");
			assertTrue("new cu does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			if (newCu != null && newCu.exists()){
				newCu.delete(true, new NullProgressMonitor());
			}
		}
	}
	
	public void testCopy_Cu_to_Itself() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
				
		ICompilationUnit newCu= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= { };
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= cu;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newCu= getPackageP().getCompilationUnit(TestCopyQueries.NEW_CU_NAME + ".java");
			assertTrue("new file does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			if (newCu != null && newCu.exists()){
				newCu.delete(true, new NullProgressMonitor());
			}
		}
	}

	public void testCopy_Cu_to_OtherPackage() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IPackageFragment otherPackage= getRoot().createPackageFragment("other.pack", true, new NullProgressMonitor());
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= otherPackage;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			ICompilationUnit newCu= otherPackage.getCompilationUnit(fileName);
			assertTrue("new file does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			otherPackage.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_Cu_to_DefaultPackage() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IPackageFragment defaultPackage= getRoot().getPackageFragment("");
		assertTrue(defaultPackage.exists());
		assertTrue(defaultPackage.isDefaultPackage());
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= defaultPackage;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			ICompilationUnit newCu= defaultPackage.getCompilationUnit(fileName);
			assertTrue("new file does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_Cu_to_SourceFolder() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		ICompilationUnit newCu= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newCu= getRoot().getPackageFragment("").getCompilationUnit(fileName);
			assertTrue("new file does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_Cu_to_JavaProject() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newFile= MySetup.getProject().getProject().getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			if (newFile != null)
				newFile.delete(true, false, null);
		}
	}

	public void testCopy_Cu_to_File_In_Package() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IFolder parentFolder= (IFolder) getPackageP().getResource();
		IFile file= parentFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);
		assertTrue(file.exists());

		ICompilationUnit newCu= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= file;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newCu= getPackageP().getCompilationUnit(TestCopyQueries.NEW_CU_NAME + ".java");
			assertTrue("new file does not exist after copying", newCu.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			file.delete(true, false, null);
			if (newCu != null && newCu.exists()){
				newCu.delete(true, new NullProgressMonitor());
			}
		}
	}

	public void testCopy_Cu_to_File_In_Resource_Folder() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IProject parentFolder= MySetup.getProject().getProject();
		IFile file= parentFolder.getFile("a.txt");
		file.create(getStream("123"), true, null);

		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= file;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newFile= MySetup.getProject().getProject().getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			file.delete(true, false, null);
			newFile.delete(true, false, null);
		}
	}
	
	public void testCopy_Cu_to_SimpleProject() throws Exception {
		String fileName= "A.java";
		ICompilationUnit cu= getPackageP().createCompilationUnit(fileName, "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());

		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		simpleProject.open(null);

		IFile newFile= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {cu};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= simpleProject;
			verifyValidDestination(ref, destination);
			
			assertTrue("source file does not exist before copying", cu.exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source file does not exist after copying", cu.exists());
			
			newFile= simpleProject.getFile(fileName);
			assertTrue("new file does not exist after copying", newFile.exists());
		} finally {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
			simpleProject.delete(true, false, null);
			newFile.delete(true, false, null);
		}
	}

	public void testCopy_Package_to_Its_Root() throws Exception {
		IPackageFragment newPackage= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= getRoot();
			verifyValidDestination(ref, destination);
			
			assertTrue("source package does not exist before copying", getPackageP().exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source package does not exist after copying", getPackageP().exists());
			
			newPackage= getRoot().getPackageFragment(TestCopyQueries.NEW_PACKAGE_NAME);
			assertTrue("new package does not exist after copying", newPackage.exists());
		} finally {
			performDummySearch();
			newPackage.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_Package_to_Itself() throws Exception {
		IPackageFragment newPackage= null;
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= getPackageP();
			verifyValidDestination(ref, destination);
			
			assertTrue("source package does not exist before copying", getPackageP().exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source package does not exist after copying", getPackageP().exists());
			
			newPackage= getRoot().getPackageFragment(TestCopyQueries.NEW_PACKAGE_NAME);
			assertTrue("new package does not exist after copying", newPackage.exists());
		} finally {
			performDummySearch();
			if (newPackage != null && newPackage.exists())
				newPackage.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_Package_to_Another_Root() throws Exception {
		IPackageFragmentRoot otherRoot= JavaProjectHelper.addSourceContainer(MySetup.getProject(), "otherRoot");
		IPackageFragment newPackage= null;
		String packageName= getPackageP().getElementName();
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= otherRoot;
			verifyValidDestination(ref, destination);
			
			assertTrue("source package does not exist before copying", getPackageP().exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source package does not exist after copying", getPackageP().exists());
			
			newPackage= otherRoot.getPackageFragment(packageName);
			assertTrue("new package does not exist after copying", newPackage.exists());
		} finally {
			performDummySearch();
			newPackage.delete(true, new NullProgressMonitor());
			otherRoot.delete(0, 0, new NullProgressMonitor());
		}
	}

	public void testCopy_Package_to_JavaProject_That_Is_Root() throws Exception {
		IJavaProject otherProject= JavaProjectHelper.createJavaProject("otherProject", null);
		JavaProjectHelper.addSourceContainer(otherProject, null);
		try {
			ICopyQueries queries= new TestCopyQueries();

			IJavaElement[] javaElements= {getPackageP()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, queries);

			Object destination= otherProject;
			verifyValidDestination(ref, destination);
			
			assertTrue("source package does not exist before copying", getPackageP().exists());
			
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source package does not exist after copying", getPackageP().exists());
			
			IPackageFragment newPackage= null;
			IPackageFragmentRoot[] roots= otherProject.getAllPackageFragmentRoots();
			for (int i= 0; i < roots.length; i++) {
				if (ReorgUtils2.isSourceFolder(roots[i])){
					newPackage= roots[i].getPackageFragment(getPackageP().getElementName());
					assertTrue("new package does not exist after copying", newPackage.exists());					
				}
			}
			assertNotNull(newPackage);
		} finally {
			performDummySearch();
			JavaProjectHelper.delete(otherProject);
		}
	}
	
	public void testCopy_folder_to_other_folder() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		IFolder otherFolder= superFolder.getFolder("otherfolder");
		otherFolder.create(true, true, null);
		
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= otherFolder;
			verifyValidDestination(ref, destination);	

			assertTrue("source does not exist before copying", folder.exists());
								
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());
			assertTrue("copied folder does not exist after copying", otherFolder.getFolder(folderName).exists());
			
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			otherFolder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testCopy_folder_Java_project() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= MySetup.getProject();
			verifyValidDestination(ref, destination);						

			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());
			
			assertTrue("copied folder does not exist after copying", MySetup.getProject().getProject().getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testCopy_folder_to_source_folder() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= getRoot();
			verifyValidDestination(ref, destination);						

			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());
			
			assertTrue("copied folder does not exist after copying", ((IFolder)getRoot().getResource()).getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}
	}

	public void testCopy_folder_to_package() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			IPackageFragment destination= getPackageP();
			verifyValidDestination(ref, destination);						
			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());			
			assertTrue("copied folder does not exist after copying", ((IFolder)destination.getResource()).getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
		}

	}
	public void testCopy_folder_to_file_in_another_folder() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		IFolder otherFolder= superFolder.getFolder("otherfolder");
		otherFolder.create(true, true, null);
		IFile fileInAnotherFolder= otherFolder.getFile("f.tex");
		fileInAnotherFolder.create(getStream("123"), true, null);

		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= fileInAnotherFolder;
			verifyValidDestination(ref, destination);						
			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());			
			assertTrue("copied folder does not exist after copying", otherFolder.getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());	
			otherFolder.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_folder_to_cu() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		ICompilationUnit cu= getPackageP().createCompilationUnit("A.java", "package p;class A{void foo(){}class Inner{}}", false, new NullProgressMonitor());
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= cu;
			verifyValidDestination(ref, destination);						
			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());			
			assertTrue("copied folder does not exist after copying", ((IFolder)getPackageP().getResource()).getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			cu.delete(true, new NullProgressMonitor());
		}
	}

	public void testCopy_folder_to_simple_project() throws Exception {
		IProject superFolder= MySetup.getProject().getProject();
		String folderName= "folder";
		IFolder folder= superFolder.getFolder(folderName);
		folder.create(true, true, null);

		IProject simpleProject= ResourcesPlugin.getWorkspace().getRoot().getProject("mySImpleProject");
		simpleProject.create(null);
		simpleProject.open(null);
		try{
			IJavaElement[] javaElements= {};
			IResource[] resources= {folder};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= simpleProject;
			verifyValidDestination(ref, destination);						
			assertTrue("source does not exist before copying", folder.exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);
			
			assertTrue("source does not exist after copying", folder.exists());			
			assertTrue("copied folder does not exist after copying", simpleProject.getFolder(folderName).exists());
		} finally{
			performDummySearch();
			folder.delete(true, new NullProgressMonitor());			
			simpleProject.delete(true, true, new NullProgressMonitor());
		}
	}

	private static IPackageFragmentRoot getSourceFolder(IJavaProject javaProject, String name) throws JavaModelException{
		IPackageFragmentRoot[] roots= javaProject.getPackageFragmentRoots();
		for (int i= 0; i < roots.length; i++) {
			if (! roots[i].isArchive() && roots[i].getElementName().equals(name))
				return roots[i];
		}
		return null;
	}
	
	public void testCopy_root_to_same_Java_project() throws Exception {
		IPackageFragmentRoot newRoot= null;
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {			};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= getRoot().getJavaProject();
			verifyValidDestination(ref, destination);
			assertTrue("source does not exist before copying", getRoot().exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);

			assertTrue("source does not exist after copying", getRoot().exists());
			String newName= "Copy of " + getRoot().getElementName();
			newRoot= getSourceFolder(MySetup.getProject(), newName);
			assertTrue("copied folder does not exist after copying", newRoot.exists());
		} finally {
			performDummySearch();
			if (newRoot != null && newRoot.exists())
				newRoot.delete(0, 0, new NullProgressMonitor());
		}
	}
	
	public void testCopy_root_to_other_Java_project() throws Exception {
		IJavaProject otherJavaProject= JavaProjectHelper.createJavaProject("other", "bin");
		try {
			IJavaElement[] javaElements= { getRoot()};
			IResource[] resources= {};
			CopyRefactoring2 ref= verifyEnabled(resources, javaElements, new TestCopyQueries());

			Object destination= otherJavaProject;
			verifyValidDestination(ref, destination);
			assertTrue("source does not exist before copying", getRoot().exists());
			RefactoringStatus status= performRefactoring(ref);
			assertEquals(null, status);

			assertTrue("source does not exist after copying", getRoot().exists());
			String newName= getRoot().getElementName();
			IPackageFragmentRoot newRoot= getSourceFolder(otherJavaProject, newName);
			assertTrue("copied folder does not exist after copying", newRoot.exists());
		} finally {
			performDummySearch();
			JavaProjectHelper.delete(otherJavaProject);
		}						
	}
}