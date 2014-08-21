package com.eztech.util;

import java.io.FileFilter;
import java.util.List;

import junit.framework.TestCase;
import test.classes.MyTagInterface;

public class JavaClassFinderTest extends TestCase {

	@SuppressWarnings("unused")
	private String myClassPath;
	private String originalClassPath;
	private String originalCustomClassPath;
	private JavaClassFinder classFinder;

	@Override
	protected void setUp() throws Exception {
		this.myClassPath = FileUtils.determineClassPathBase(this.getClass());
		this.classFinder = new JavaClassFinder();
		this.originalClassPath = System.getProperty("java.class.path");
		this.originalCustomClassPath = System
				.getProperty(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY);
		if (this.originalCustomClassPath != null) {
			System.out.println("custom classpath was already set to="
					+ this.originalCustomClassPath);
		}
		System.out.println("original classpath=" + this.originalClassPath);
	}

	@Override
	protected void tearDown() throws Exception {
		System.setProperty("java.class.path", this.originalClassPath);
		if (this.originalCustomClassPath != null) {
			System.setProperty(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY,
					this.originalCustomClassPath);
			System.out.println("custom classpath sysproperty reset to="
					+ this.originalCustomClassPath);
		} else {
			System.out
					.println("original custom classpath sysproperty was blank, clearing any custom classpath value value set in tests");
			System.clearProperty(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY);
		}
		// System.getProperties().remove(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY);
	}

	public void testFindExpectedJavaClassFilesForOneFile() throws Throwable {
		List<Class<? extends MyTagInterface>> classes = this.classFinder
				.findAllMatchingTypes(MyTagInterface.class, null);
		assertEquals("found the class", 3, classes.size());

	}

	public void testFindSubclassesClasses() throws Exception {
		Class<FileWalker> toMatch = FileWalker.class;
		String classPathRoot = FileUtils.determineClassPathBase(toMatch);
		System.setProperty("custom.class.path", classPathRoot);
		List<Class<? extends FileWalker>> classes = this.classFinder
				.findAllMatchingTypes(toMatch, null);
		assertEquals("found the subclasses of " + toMatch, 2, classes.size());
		assertTrue(classes.contains(JavaClassFileWalker.class));
		assertTrue(classes.contains(FileWalker.class));

		assertTrue(FileFilter.class.isAssignableFrom(JavaClassFileFilter.class));
	}

	public void testAbilityToUseCustomClassPathProperty() throws Exception {
		Class<FileWalker> toMatch = FileWalker.class;
		String classPathRoot = FileUtils.determineClassPathBase(toMatch);
		System.setProperty(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY,
				classPathRoot);
		List<Class<? extends FileWalker>> classes = this.classFinder
				.findAllMatchingTypes(toMatch, null);
		assertEquals("found the subclasses of " + toMatch, 2, classes.size());
		assertTrue(classes.contains(JavaClassFileWalker.class));
		assertTrue(classes.contains(FileWalker.class));

		assertTrue(FileFilter.class.isAssignableFrom(JavaClassFileFilter.class));
	}

}
