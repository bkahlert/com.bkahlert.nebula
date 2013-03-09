package com.bkahlert.devel.nebula.views;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;

public class SampleEditorView extends EditorView<String> {

	public SampleEditorView() {
		super(500, ToolbarSet.DEFAULT, true);
	}

	@Override
	public void postInit() {
		this.load("Hello World!");
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SampleEditorView.this.load(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 4000);
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SampleEditorView.this.load("Second World!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 8000);
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
