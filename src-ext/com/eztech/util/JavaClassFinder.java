package com.eztech.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to walk the Java classpath, and to find all classes which are
 * assignable (i.e. inherit) a specified class. If no matching class is
 * specified, will return all classes in the classpath
 * 
 * @author Sam
 * 
 */
public class JavaClassFinder {
	public static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";
	public static final String CUSTOM_CLASS_PATH_PROPERTY = "custom.class.path";

	// private static Logger LOG = Logger.getLogger(JavaClassFinder.class);

	private ArrayList<Class<?>> foundClasses;
	private Class<?> toFind;
	private JavaClassFileWalker fileWalker;
	private ClassLoadingFileHandler fileHandler;

	/**
	 * Finds all classes which are Assignable from the specified class
	 * 
	 * @param toFind
	 *            only classes which are subtypes or implementers of the this
	 *            class are found
	 * @param filter
	 *            to be used to select packages and class files to be taken into
	 *            account
	 * @return List of class objects
	 */
	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> findAllMatchingTypes(Class<T> toFind,
			FileFilter filter) {
		this.foundClasses = new ArrayList<Class<?>>();
		List<Class<? extends T>> returnedClasses = new ArrayList<Class<? extends T>>();
		this.toFind = toFind;
		this.walkClassPath(filter);
		for (Class<?> clazz : this.foundClasses) {
			returnedClasses.add((Class<? extends T>) clazz);
		}
		return returnedClasses;
	}

	private void walkClassPath(FileFilter filter) {
		this.fileHandler = new ClassLoadingFileHandler();
		this.fileWalker = new JavaClassFileWalker(this.fileHandler);

		String[] classPathRoots = this.getClassPathRoots();
		for (int i = 0; i < classPathRoots.length; i++) {
			String path = classPathRoots[i];
			if (path.endsWith(".jar")) {
				// LOG.warn("walkClassPath(): reading from jar not yet implemented, jar file="
				// + path);
				continue;
			}
			// LOG.debug("walkClassPath(): checking classpath root: " + path);
			// have to reset class path base so it can instance classes properly
			this.fileHandler.updateClassPathBase(path);
			this.fileWalker.setBaseDir(path);
			this.fileWalker.walk(filter);
		}
	}

	public String[] getClassPathRoots() {
		String classPath;
		if (System.getProperties().containsKey(CUSTOM_CLASS_PATH_PROPERTY)) {
			// LOG.debug("getClassPathRoots(): using custom classpath property to search for classes");
			classPath = System.getProperty(CUSTOM_CLASS_PATH_PROPERTY);
		} else {
			classPath = System.getProperty(JAVA_CLASS_PATH_PROPERTY);
		}
		String[] pathElements = classPath.split(File.pathSeparator);
		// LOG.debug("getClassPathRoots(): classPath roots=" +
		// StringUtil.dumpArray(pathElements));
		return pathElements;
	}

	private void handleClass(Class<?> clazz) {
		boolean isMatch = false;
		isMatch = this.toFind == null || this.toFind.isAssignableFrom(clazz);
		if (isMatch) {
			this.foundClasses.add(clazz);
		}
	}

	/**
	 * FileFindHandler plugin for the JavaClassFileWalker object to create a
	 * class object for matched class files
	 * 
	 * @author Sam
	 * 
	 */
	class ClassLoadingFileHandler extends FileFindHandlerAdapter {
		private FileToClassConverter converter;

		public void updateClassPathBase(String classPathRoot) {
			if (this.converter == null) {
				this.converter = new FileToClassConverter(classPathRoot);
			}
			this.converter.setClassPathRoot(classPathRoot);
		}

		@Override
		public void handleFile(File file) {
			// if we get a Java class file, try to convert it to a class
			Class<?> clazz = this.converter.convertToClass(file);
			if (clazz == null) {
				return;
			}
			JavaClassFinder.this.handleClass(clazz);
		}
	}

	/**
	 * Finds all classes in the classpath
	 * 
	 * @param filter
	 *            to be used to select packages and class files to be taken into
	 *            account
	 * @return
	 */
	public List<Class<?>> findAllMatchingTypes(FileFilter filter) {
		return this.findAllMatchingTypes(null, filter);
	}

	public int getScannedClassesCount() {
		if (this.fileWalker == null) {
			return 0;
		}
		return this.fileWalker.getAllFilesCount();
	}

}
