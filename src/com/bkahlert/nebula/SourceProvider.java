package com.bkahlert.nebula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class SourceProvider extends AbstractSourceProvider {

	public static final Object NULL_MANAGER = new Object();
	public static final Object NULL_CONTROL = new Object();
	public static final Object NULL_INPUT = new Object();

	private static final List<SourceProvider> INSTANCES = new ArrayList<SourceProvider>();

	// TODO not called yet
	public static void managerChanged(Object manager) {
		if (manager == null) {
			manager = NULL_MANAGER;
		}
		for (SourceProvider sourceProvider : INSTANCES) {
			sourceProvider.fireManagerChanged(manager);
		}
	}

	public static void controlChanged(Object control) {
		// System.err.println(control);
		if (control == null) {
			control = NULL_CONTROL;
		}
		for (SourceProvider sourceProvider : INSTANCES) {
			sourceProvider.fireControlChanged(control);
		}
	}

	public static void inputChanged(Object input) {
		if (input == null) {
			input = NULL_INPUT;
		}
		for (SourceProvider sourceProvider : INSTANCES) {
			sourceProvider.fireInputChanged(input);
		}
	}

	/**
	 * Corresponds to a serviceProvider variable as defined in Extension
	 * org.eclipse.ui.services.
	 */
	public static final String MANAGER = "com.bkahlert.nebula.information.manager";

	/**
	 * Corresponds to a serviceProvider variable as defined in Extension
	 * org.eclipse.ui.services.
	 */
	public static final String CONTROL = "com.bkahlert.nebula.information.control";

	/**
	 * Corresponds to a serviceProvider variable as defined in Extension
	 * org.eclipse.ui.services.
	 */
	public static final String INPUT = "com.bkahlert.nebula.information.input";

	protected Object manager;
	protected Object control;
	protected Object input;

	public SourceProvider() {
		this.fireSourceChanged(ISources.WORKBENCH, MANAGER, this.manager);
		this.fireSourceChanged(ISources.WORKBENCH, CONTROL, this.control);
		this.fireSourceChanged(ISources.WORKBENCH, INPUT, this.input);
		INSTANCES.add(this);
	}

	@Override
	public void dispose() {
		INSTANCES.remove(INSTANCES);
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { MANAGER, CONTROL, INPUT };
	}

	@Override
	public Map<Object, Object> getCurrentState() {
		Map<Object, Object> map = new HashMap<Object, Object>(3);
		map.put(MANAGER, this.manager);
		map.put(CONTROL, this.control);
		map.put(INPUT, this.input);
		return map;
	}

	private final void fireManagerChanged(final Object manager) {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				SourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
						MANAGER, manager);
			}
		});
	}

	private final void fireControlChanged(final Object control) {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				SourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
						CONTROL, control);
			}
		});
	}

	private final void fireInputChanged(final Object input) {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				SourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
						INPUT, input);
			}
		});
	}
}
