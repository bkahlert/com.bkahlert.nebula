package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class WorkbenchUtils {
	private static final Logger LOGGER = Logger.getLogger(WorkbenchUtils.class);

	/**
	 * Returns a {@link IViewPart} with the given id.
	 * <p>
	 * This method may be called from any thread.
	 * 
	 * @param id
	 * @return
	 */
	public static IViewPart getView(final String id) {
		Callable<IViewPart> callable = new Callable<IViewPart>() {
			@Override
			public IViewPart call() throws Exception {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage()
						.showView(id, null, IWorkbenchPage.VIEW_VISIBLE);
			}
		};
		try {
			if (Display.getCurrent() == Display.getDefault()) {
				return callable.call();
			} else {
				return ExecUtils.syncExec(callable);
			}
		} catch (Exception e) {
			LOGGER.error("Error retrieving " + IViewPart.class.getSimpleName(),
					e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends IViewPart> List<T> getViews(final Class<T> clazz) {
		try {
			return ExecUtils.syncExec(new Callable<List<T>>() {
				@Override
				public List<T> call() throws Exception {
					List<T> viewParts = new ArrayList<T>();
					for (IWorkbenchPage page : PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getPages()) {
						for (IViewReference viewReference : page
								.getViewReferences()) {
							IViewPart part = (IViewPart) viewReference
									.getPart(true);
							if (clazz.isInstance(part)) {
								viewParts.add((T) part);
							}
						}
					}
					return viewParts;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static IViewPart getView(final String id, final boolean create) {
		try {
			return ExecUtils.syncExec(new Callable<IViewPart>() {
				@Override
				public IViewPart call() throws Exception {
					for (IWorkbenchPage page : PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getPages()) {
						for (IViewReference viewReference : page
								.getViewReferences()) {
							if (viewReference.getId().equals(id)) {
								return (IViewPart) viewReference
										.getPart(create);
							}
						}
					}
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static List<IViewPart> getViews(final String id, final boolean create) {
		try {
			return ExecUtils.syncExec(new Callable<List<IViewPart>>() {
				@Override
				public List<IViewPart> call() throws Exception {
					List<IViewPart> viewParts = new ArrayList<IViewPart>();
					for (IWorkbenchPage page : PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getPages()) {
						for (IViewReference viewReference : page
								.getViewReferences()) {
							if (viewReference.getId().equals(id)) {
								viewParts.add((IViewPart) viewReference
										.getPart(create));
							}
						}
					}
					return viewParts;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
