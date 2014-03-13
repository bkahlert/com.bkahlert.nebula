package com.bkahlert.nebula.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.editor.Editor;

public class SampleEditorView extends EditorView<String> {

	public SampleEditorView() {
		super(500, ToolbarSet.DEFAULT, true);
	}

	@Override
	public void postInit() {
		this.load(null, "Hello World!");
		// ExecUtils.asyncExec(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// SampleEditorView.this.load((String) null);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }, 4000);
		// ExecUtils.asyncExec(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// SampleEditorView.this.load("Input");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }, 8000);
		// ExecUtils.asyncExec(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// SampleEditorView.this.load("Input #1", "Input #2");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }, 12000);

		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SampleEditorView.this.load(null, "Input #1", "Input #2");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	@Override
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("Sample Editor", null);
	}

	@Override
	public PartInfo getPartInfo(List<String> loadedObjects) {
		return new PartInfo("Sample Editor for " + loadedObjects, null);
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

	@Override
	public void created(List<Editor<String>> editors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disposed(List<Editor<String>> editors) {
		// TODO Auto-generated method stub

	}

}
