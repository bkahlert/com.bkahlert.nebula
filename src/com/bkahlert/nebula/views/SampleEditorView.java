package com.bkahlert.nebula.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.editor.Editor;

public class SampleEditorView extends EditorView<String> {

	public SampleEditorView() {
		super(returnValue -> new Pair<String, org.eclipse.swt.graphics.Image>(
				returnValue, null), 500, ToolbarSet.DEFAULT, true);
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

		ExecUtils.asyncExec(() -> {
			try {
				SampleEditorView.this.load(null, "Input #1", "Input #2");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 1000);
	}

	@Override
	public String getTitle(String objectToLoad, IProgressMonitor monitor)
			throws Exception {
		return objectToLoad;
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
