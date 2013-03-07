package com.bkahlert.devel.nebula.views;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class SampleEditorView extends EditorView<String> {

	public SampleEditorView() {
		super(500, true);
	}

	@Override
	public void postInit() {
		this.load("Hello World!");
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					load(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 2000);
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					load("Second World!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 5000);
	}

	@Override
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("Sample Editor", null);
	}

	@Override
	public PartInfo getPartInfo(String loadedObject) {
		return new PartInfo("Sample Editor for " + loadedObject, null);
	}

	@Override
	public String getHtml(String objectToLoad, IProgressMonitor monitor) {
		return objectToLoad;
	}

	@Override
	public void setHtml(String loadedObject, String html,
			IProgressMonitor monitor) {
		System.out.println("saved: " + html);
	}

}
