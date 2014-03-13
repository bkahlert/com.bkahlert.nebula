package com.bkahlert.nebula.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.editor.AutosaveEditor;
import com.bkahlert.nebula.widgets.editor.Editor;

/**
 * Instances of this class are {@link ViewPart}s that wrap a {@link Editor}.
 * 
 * @param <T>
 *            type of the objects that can be loaded in the wrapped
 *            {@link Editor}
 * 
 * @author bkahlert
 */
public abstract class EditorView<T> extends ViewPart {

	private static final Logger LOGGER = Logger.getLogger(EditorView.class);

	public static class PartInfo {
		private final String title;
		private final Image image;

		public PartInfo(String title, Image image) {
			super();
			this.title = title;
			this.image = image;
		}

		/**
		 * @return the part's title
		 */
		public String getTitle() {
			return this.title;
		}

		/**
		 * @return the part's image
		 */
		public Image getImage() {
			return this.image;
		}
	}

	private final long delayChangeEventUpTo;
	private final ToolbarSet toolbarSet;
	private final boolean autosave;

	private Composite parent;
	private final List<Editor<T>> editors;

	/**
	 * Creates a new instance using a classic {@link Editor} or a
	 * {@link AutosaveEditor}.
	 * 
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order save the
	 *            currently loaded object. If 0 no delay will be applied. The
	 *            minimal delay however defined by the wrapped {@link Image}.
	 * @param autosave
	 */
	public EditorView(long delayChangeEventUpTo, ToolbarSet toolbarSet,
			boolean autosave) {
		this.delayChangeEventUpTo = delayChangeEventUpTo;
		this.toolbarSet = toolbarSet;
		this.autosave = autosave;
		this.parent = null;
		this.editors = new ArrayList<Editor<T>>(2);
	}

	public List<Editor<T>> getEditors() {
		return new ArrayList<Editor<T>>(this.editors);
	}

	protected void refreshHeader() {
		final AtomicReference<PartInfo> partInfo = new AtomicReference<PartInfo>();
		List<T> loadedObjects = this.getLoadedObjects();
		if (loadedObjects.size() > 0) {
			try {
				partInfo.set(this.getPartInfo(loadedObjects));
			} catch (Exception e) {
				LOGGER.error("Error while refreshing header", e);
				partInfo.set(new PartInfo("ERROR", PlatformUI.getWorkbench()
						.getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_ERROR_TSK)));
			}
		} else {
			partInfo.set(this.getDefaultPartInfo());
		}

		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				EditorView.this.setPartName(partInfo != null
						&& partInfo.get().getTitle() != null ? partInfo.get()
						.getTitle() : "Editor");
				EditorView.this.setTitleImage(partInfo != null
						&& partInfo.get().getImage() != null ? partInfo.get()
						.getImage() : null);
			}
		});
	}

	/**
	 * Returns the {@link PartInfo} to be used when no object is loaded.
	 * 
	 * @return
	 */
	public abstract PartInfo getDefaultPartInfo();

	/**
	 * Returns the {@link PartInfo} is the given object is loaded.
	 * 
	 * @param loadedObject
	 * @return
	 */
	public abstract PartInfo getPartInfo(List<T> loadedObjects)
			throws Exception;

	@Override
	public final void createPartControl(Composite parent) {
		this.parent = parent;
		this.parent.setLayout(new FillLayout());

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});

		this.postInit();
	}

	/**
	 * This method is called after this {@link EditorView} has been initialized.
	 */
	public void postInit() {
		return;
	}

	/**
	 * 
	 * @param objectsToLoad
	 * @see Editor#load(Object)
	 */
	public final void load(final Runnable callback, T... objectsToLoad) {

		this.createEditors(objectsToLoad.length);
		Assert.isTrue(objectsToLoad.length == this.editors.size());

		if (objectsToLoad.length == 0) {
			EditorView.this.refreshHeader();
			if (callback != null) {
				callback.run();
			}
			return;
		}

		final AtomicReference<Integer> jobCount = new AtomicReference<Integer>(
				0);
		for (int i = 0; i < objectsToLoad.length; i++) {
			NamedJob loadJob = this.editors.get(i).load(objectsToLoad[i]);
			if (loadJob != null) {
				jobCount.set(jobCount.get() + 1);
				loadJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						jobCount.set(jobCount.get() - 1);
						if (jobCount.get() == 0) {
							EditorView.this.refreshHeader();
							if (callback != null) {
								callback.run();
							}
						}
					}
				});
			}
		}
	}

	private void createEditors(int length) {
		List<Editor<T>> disposed = new ArrayList<Editor<T>>();
		List<Editor<T>> created = new ArrayList<Editor<T>>();
		while (length < this.editors.size()) {
			this.editors.get(length).dispose();
			disposed.add(this.editors.remove(length));
		}
		while (length > this.editors.size()) {
			Editor<T> editor;
			if (this.autosave) {
				editor = new AutosaveEditor<T>(this.parent, SWT.NONE,
						this.delayChangeEventUpTo, this.toolbarSet) {
					@Override
					public String getHtml(T loadedObject,
							IProgressMonitor monitor) throws Exception {
						return EditorView.this.getHtml(loadedObject, monitor);
					}

					@Override
					public void setHtml(T loadedObject, String html,
							IProgressMonitor monitor) throws Exception {
						EditorView.this.setHtml(loadedObject, html, monitor);
					}
				};
			} else {
				editor = new Editor<T>(this.parent, SWT.NONE,
						this.delayChangeEventUpTo, this.toolbarSet) {
					@Override
					public String getHtml(T loadedObject,
							IProgressMonitor monitor) throws Exception {
						return EditorView.this.getHtml(loadedObject, monitor);
					}

					@Override
					public void setHtml(T loadedObject, String html,
							IProgressMonitor monitor) throws Exception {
						EditorView.this.setHtml(loadedObject, html, monitor);
					}
				};
			}
			this.editors.add(editor);
			created.add(editor);
		}
		this.parent.layout();
		if (disposed.size() > 0) {
			this.disposed(created);
		}
		if (created.size() > 0) {
			this.created(created);
		}
	}

	public abstract void created(List<Editor<T>> editors);

	public abstract void disposed(List<Editor<T>> editors);

	/**
	 * @throws Exception
	 * @see {@link Editor#save()}
	 */
	public final void save() throws Exception {
		for (Editor<T> editor : this.editors) {
			if (!editor.isDisposed()) {
				editor.save();
			}
		}
	}

	/**
	 * @return
	 * @see {@link Editor#getLoadedObject()}
	 */
	public List<T> getLoadedObjects() {
		List<T> loadedObjects = new ArrayList<T>();
		for (int i = 0; i < this.editors.size(); i++) {
			loadedObjects.add(this.editors.get(i).getLoadedObject());
		}
		return loadedObjects;
	}

	/**
	 * Returns the html for the given object.
	 * 
	 * @param objectToLoad
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	public abstract String getHtml(T objectToLoad, IProgressMonitor monitor)
			throws Exception;

	/**
	 * Sets the given html to the loaded object.
	 * 
	 * @param loadedObject
	 * @param html
	 * @param monitor
	 */
	public abstract void setHtml(T loadedObject, String html,
			IProgressMonitor monitor) throws Exception;

	@Override
	public void setFocus() {
		if (this.editors.size() > 0 && this.editors.get(0) != null
				&& !this.editors.get(0).isDisposed()) {
			this.editors.get(0).setFocus();
		}
	}

}
