package com.bkahlert.nebula.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import com.eztech.util.JavaClassFinder;

/**
 * 
 * @author bkahlert
 * 
 * @see <a
 *      href="https://github.com/bmc/javautil/blob/master/src/main/java/org/clapper/util/classutil/ClassFinder.java">ClassFinder</a>
 */
public class ClassUtils {

	/**
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class ActivationFileFilter implements FileFilter {

		private final FileFilter innerFilter;

		public ActivationFileFilter(FileFilter innerFilter) {
			this.innerFilter = innerFilter;
		}

		public ActivationFileFilter() {
			this(null);
		}

		@Override
		public boolean accept(File pathname) {
			System.err.println(pathname);
			String[] filteredPackages = new String[] { "/org/osgi", "/javax" };
			String[] filteredFiles = new String[] { "Loader.class",
					"Activator.class", "ImageManager.class" };
			for (String filteredPackage : filteredPackages) {
				if (pathname.isDirectory()
						&& pathname.toString().contains(filteredPackage)) {
					return false;
				}
			}
			for (String filteredFile : filteredFiles) {
				if (pathname.isFile()
						&& pathname.toString().endsWith(filteredFile)) {
					return false;
				}
			}
			return this.innerFilter != null ? this.innerFilter.accept(pathname)
					: true;
		}
	};

	/**
	 * Does not find inner classes and some locations (like the osgi package)
	 * are automatically filtered.
	 * 
	 * @param interfaze
	 * @param filter
	 * @return
	 */
	public static <T> List<Class<? extends T>> getImplementingTypes(
			Class<T> interfaze, FileFilter filter) {
		return new JavaClassFinder().findAllMatchingTypes(interfaze,
				new ActivationFileFilter(filter));
	}

	/**
	 * Does not find inner classes and some locations (like the osgi package)
	 * are automatically filtered.
	 * 
	 * @param interfaze
	 * @return
	 */
	public static <T> List<Class<? extends T>> getImplementingTypes(
			Class<T> interfaze) {
		return getImplementingTypes(interfaze, null);
	}
}
