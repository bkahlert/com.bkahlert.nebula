package com.bkahlert.nebula.views;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
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
	private Editor<T> editor;

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
	}

	public Editor<T> getEditor() {
		return this.editor;
	}

	protected void refreshHeader() {
		final AtomicReference<PartInfo> partInfo = new AtomicReference<PartInfo>();
		if (this.getEditor().getLoadedObject() != null) {
			try {
				partInfo.set(this.getPartInfo(this.getEditor()
						.getLoadedObject()));
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
	public abstract PartInfo getPartInfo(T loadedObject) throws Exception;

	@Override
	public final void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		if (this.autosave) {
			this.editor = new AutosaveEditor<T>(parent, SWT.NONE,
					this.delayChangeEventUpTo, this.toolbarSet) {
				@Override
				public String getHtml(T loadedObject, IProgressMonitor monitor)
						throws Exception {
					return EditorView.this.getHtml(loadedObject, monitor);
				}

				@Override
				public void setHtml(T loadedObject, String html,
						IProgressMonitor monitor) throws Exception {
					EditorView.this.setHtml(loadedObject, html, monitor);
				}
			};
		} else {
			this.editor = new Editor<T>(parent, SWT.NONE,
					this.delayChangeEventUpTo, this.toolbarSet) {
				@Override
				public String getHtml(T loadedObject, IProgressMonitor monitor)
						throws Exception {
					return EditorView.this.getHtml(loadedObject, monitor);
				}

				@Override
				public void setHtml(T loadedObject, String html,
						IProgressMonitor monitor) throws Exception {
					EditorView.this.setHtml(loadedObject, html, monitor);
				}
			};
		}

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
	 * @param objectToLoad
	 * @see Editor#load(Object)
	 */
	public final void load(T objectToLoad) {
		if (!this.getEditor().isDisposed()) {
			NamedJob loadJob = this.getEditor().load(objectToLoad);
			if (loadJob != null) {
				loadJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						EditorView.this.refreshHeader();
					}
				});
			} else {
				EditorView.this.refreshHeader();
			}
		}
	}

	/**
	 * @throws Exception
	 * @see {@link Editor#save()}
	 */
	public final void save() throws Exception {
		if (!this.getEditor().isDisposed()) {
			this.getEditor().save();
		}
	}

	/**
	 * @return
	 * @see {@link Editor#getLoadedObject()}
	 */
	public T getLoadedObject() {
		return this.getEditor().getLoadedObject();
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
		if (this.editor != null && !this.editor.isDisposed()) {
			this.editor.setFocus();
		}
	}

}
