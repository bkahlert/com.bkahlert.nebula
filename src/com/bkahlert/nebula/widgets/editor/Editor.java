package com.bkahlert.nebula.widgets.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.nebula.utils.EventDelegator;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.composer.Composer;
import com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;

/**
 * Instances of this class wrap a {@link BootstrapBrowser} and add load and save
 * functionality.
 * <p>
 * If multiple {@link Editor}s loaded the same information, only the one in
 * focus saved its changed whereas the other reflect them.
 * 
 * @param <T>
 *            type of the objects that can be loaded in this {@link Editor}
 * 
 * @author bkahlert
 * 
 */
public abstract class Editor<T> extends Composite {

	private static final Logger LOGGER = Logger.getLogger(Editor.class);

	/**
	 * Reference to the {@link Editor} that executed the last save action.
	 * <p>
	 * Used to distinguish the saving {@link AbstractMemoView} from the others
	 * which need to reload their contents if they loaded the same object.
	 */
	private static Map<Object, List<Editor<Object>>> responsibleEditors = new HashMap<Object, List<Editor<Object>>>();
	private static Editor<?> lastFocussedEditor = null;

	private T loadedObject = null;
	private NamedJob loadJob = null;
	private final Map<T, Job> saveJobs = new HashMap<T, Job>();

	protected Composer composer = null;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order save the
	 *            currently loaded object. If 0 no delay will be applied. The
	 *            minimal delay however defined by the wrapped {@link Image}.
	 * @param toolbarSet
	 */
	public Editor(Composite parent, int style, final long delayChangeEventUpTo,
			ToolbarSet toolbarSet) {
		super(parent, style & ~SWT.BORDER);

		this.setLayout(new FillLayout());
		this.composer = new Composer(this, style & SWT.BORDER,
				delayChangeEventUpTo, toolbarSet);
		this.composer.setEnabled(false);
		this.composer.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				lastFocussedEditor = Editor.this;
			}
		});
		this.composer.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				try {
					Editor.this.save();
				} catch (Exception e1) {
					LOGGER.error(
							"Error saving content of " + Editor.this.getClass(),
							e1);
				}
			}
		});
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		if (EventDelegator.mustDelegate(eventType, this)) {
			this.composer.addListener(eventType, listener);
		} else {
			super.addListener(eventType, listener);
		}
	}

	/**
	 * @param ankerListener
	 * @see com.bkahlert.nebula.widgets.browser.Browser#addAnkerListener(com.bkahlert.nebula.widgets.browser.listener.IAnkerListener)
	 */
	public void addAnkerListener(IAnkerListener ankerListener) {
		this.composer.addAnkerListener(ankerListener);
	}

	/**
	 * @param ankerListener
	 * @see com.bkahlert.nebula.widgets.browser.Browser#removeAnkerListener(com.bkahlert.nebula.widgets.browser.listener.IAnkerListener)
	 */
	public void removeAnkerListener(IAnkerListener ankerListener) {
		this.composer.removeAnkerListener(ankerListener);
	}

	/**
	 * @param ankerLabelProvider
	 * @see com.bkahlert.nebula.widgets.composer.Composer#addAnkerLabelProvider(com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider)
	 */
	public void addAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
		this.composer.addAnkerLabelProvider(ankerLabelProvider);
	}

	/**
	 * @param ankerLabelProvider
	 * @see com.bkahlert.nebula.widgets.composer.Composer#removeAnkerLabelProvider(com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider)
	 */
	public void removeAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
		this.composer.removeAnkerLabelProvider(ankerLabelProvider);
	}

	/**
	 * 
	 * @see com.bkahlert.nebula.widgets.composer.Composer#showSource()
	 */
	public void showSource() {
		this.composer.showSource();
	}

	/**
	 * 
	 * @see com.bkahlert.nebula.widgets.composer.Composer#hideSource()
	 */
	public void hideSource() {
		this.composer.hideSource();
	}

	@Override
	public void setBackground(Color color) {
		this.composer.setBackground(color);
	}

	@Override
	public boolean setFocus() {
		lastFocussedEditor = this;
		return this.composer.setFocus();
	}

	public T getLoadedObject() {
		return this.loadedObject;
	}

	/**
	 * Loads the given object and immediately returns.
	 * <p>
	 * May safely be called multiple times. If this {@link Editor} is still
	 * loading the current load job is cancelled.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @param objectToLoad
	 * @return the {@link Job} used to load the object; null if no object to be
	 *         loaded.
	 * @throws Exception
	 */
	public final NamedJob load(final T objectToLoad) {
		if (responsibleEditors.get(this.loadedObject) != null) {
			responsibleEditors.get(this.loadedObject).remove(this);
			if (responsibleEditors.get(this.loadedObject).isEmpty()) {
				responsibleEditors.remove(this.loadedObject);
			}
		}

		if (this.loadedObject == objectToLoad) {
			return null;
		}

		if (this.loadJob != null) {
			this.loadJob.cancel();
		}

		if (objectToLoad == null) {
			// refreshHeader();
			this.loadedObject = null;
			ExecUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					Editor.this.composer.setSource("");
					Editor.this.composer.setEnabled(false);
				}
			});
			return null;
		} else {
			this.loadJob = new NamedJob(Editor.class, "Loading " + objectToLoad) {
				@SuppressWarnings("unchecked")
				@Override
				protected IStatus runNamed(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled()) {
						Editor.this.loadedObject = null;
						return Status.CANCEL_STATUS;
					}
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 3);
					final AtomicReference<String> html = new AtomicReference<String>();
					try {
						html.set(Editor.this.getHtml(objectToLoad,
								monitor.newChild(1)));

						// refreshHeader();
						monitor.worked(1);
						Future<Boolean> success = Editor.this.composer
								.setSource(html.get());
						if (!success.get()) {
							throw new Exception(
									"Could not set source's content");
						}
						monitor.worked(1);
						monitor.done();
						Editor.this.loadedObject = objectToLoad;
						if (responsibleEditors.get(objectToLoad) == null) {
							responsibleEditors.put(objectToLoad,
									new ArrayList<Editor<Object>>());
						}
						responsibleEditors.get(objectToLoad).add(
								(Editor<Object>) Editor.this);

						ExecUtils.syncExec(new Runnable() {
							@Override
							public void run() {
								Editor.this.composer.setEnabled(true);
							}
						});
					} catch (Exception e) {
						LOGGER.error("Error while loading content of "
								+ objectToLoad, e);
						Editor.this.loadedObject = null;
						return Status.CANCEL_STATUS;
					}

					return Status.OK_STATUS;
				}
			};
			this.loadJob.schedule();
			return this.loadJob;
		}
	}

	/**
	 * Saves the current content of the {@link Editor} to the loaded object.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @return the {@link Job} used to save the object.
	 * @throws Exception
	 */
	public final Job save() throws Exception {
		String html = ExecUtils.syncExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Editor.this.composer.getSource();
			}
		});
		return this.save(html);
	}

	/**
	 * Saves the given html to the loaded object.
	 * <p>
	 * In contrast to {@link #save()} this method does not use the
	 * {@link Editor}'s content and thus also works if this widget is being
	 * disposed.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @param html
	 * @return the {@link Job} used to save the object.
	 */
	synchronized Job save(final String html) {
		if (this.loadedObject == null) {
			return null;
		}

		if (lastFocussedEditor != this) {
			return null;
		}

		if (responsibleEditors.get(this.loadedObject) != null) {
			for (Editor<Object> responsibleEditor : responsibleEditors
					.get(this.loadedObject)) {
				if (responsibleEditor != this) {
					responsibleEditor.composer.setSource(html);
				}
			}
		}

		final T savedLoadedObject = this.loadedObject;
		if (this.saveJobs.get(savedLoadedObject) != null) {
			this.saveJobs.get(savedLoadedObject).cancel();
		}

		// make the loaded object still accessible even if another one has been
		// loaded already so the save job can finish

		Job saveJob = new Job("Saving " + savedLoadedObject) {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 2);
				monitor.worked(1);
				try {
					Editor.this.setHtml(savedLoadedObject, html,
							monitor.newChild(1));
				} catch (Exception e) {
					LOGGER.error("Error while saving content of "
							+ savedLoadedObject, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		saveJob.schedule();
		this.saveJobs.put(savedLoadedObject, saveJob);
		return saveJob;
	}

	/**
	 * Returns the html for the given object.
	 * 
	 * @param objectToLoad
	 * @param monitor
	 * @return
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

}
