package com.eztech.util;

import java.io.File;

/**
 * Convert a File object to a Class
 * 
 * @author Sam
 * 
 */
public class FileToClassConverter {

	private String classPathRoot;

	public FileToClassConverter(String classPathRoot) {
		this.setClassPathRoot(classPathRoot);
	}

	/**
	 * @param classPathRoot
	 */
	public void setClassPathRoot(String classPathRoot) {
		if (classPathRoot == null) {
			throw new RuntimeException("Class path root must not be null");
		}
		this.classPathRoot = classPathRoot;
	}

	@SuppressWarnings("rawtypes")
	public Class convertToClass(File classFile) {
		Class classInstance = null;
		if (classFile.getAbsolutePath().startsWith(this.classPathRoot)
				&& classFile.getAbsolutePath().endsWith(".class")) {
			classInstance = this.getClassFromName(classFile.getAbsolutePath());
		}
		return classInstance;
	}

	@SuppressWarnings("rawtypes")
	private Class getClassFromName(String fileName) {
		try {
			String className = this.removeClassPathBase(fileName);
			className = FileUtils.removeExtension(className);
			return Class.forName(className);
		} catch (Throwable e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param fileName
	 * @return
	 */
	private String removeClassPathBase(String fileName) {
		String classPart = fileName.substring(this.classPathRoot.length() + 1);
		String className = classPart.replace(File.separatorChar, '.');
		return className;
	}

}
