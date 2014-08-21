package com.eztech.util;

import java.io.File;
import java.io.FileFilter;

public class FileWalker {

	private FileFilter matchFilter;

	private FileFindHandler handler;

	private String baseDir;

	private int matchingFiles;

	private int allFiles;

	public FileWalker() {
		this(new MatchAllFileFilter(), new FileFindHandlerAdapter());
	}

	public FileWalker(FileFilter matchFilter) {
		this(matchFilter, new FileFindHandlerAdapter());
	}

	public FileWalker(FileFindHandler handler) {
		this(new MatchAllFileFilter(), handler);
	}

	public FileWalker(FileFilter matchFilter, FileFindHandler handler) {
		this.matchFilter = matchFilter;
		this.handler = handler;
	}

	public FileFilter getMatchFilter() {
		return this.matchFilter;
	}

	public void setMatchFilter(FileFilter matchFilter) {
		this.matchFilter = matchFilter;
	}

	public FileFindHandler getHandler() {
		return this.handler;
	}

	public void setHandler(FileFindHandler handler) {
		this.handler = handler;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public void walk(FileFilter filter) {
		try {
			File rootDir = new File(this.baseDir);
			this.walk(rootDir, filter);
			// notify handler that we are done walking
			this.handler.onComplete();
		} catch (Exception e) {
			// if any sort of error occurs due to bad file path, or other
			// issues, just catch and swallow
			// because we don't expect any Exceptions in normal course of usage
			e.printStackTrace();
		}
	}

	/**
	 * Preorder traversal of tree
	 * 
	 * @param rootDir
	 */
	protected void walk(File currentDir, FileFilter filter) {
		File[] files = currentDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (filter != null && !filter.accept(file)) {
				continue;
			}

			if (file.isDirectory()) {
				this.walk(file, filter);
			} else {

				if (this.matchFilter.accept(file)) {
					this.matchingFiles++;
					this.handler.handleFile(file);
				}
				this.allFiles++;
			}
		}

	}

	public int getMatchingFileCount() {
		return this.matchingFiles;
	}

	public int getAllFilesCount() {
		return this.allFiles;
	}

}
