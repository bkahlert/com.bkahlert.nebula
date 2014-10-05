package com.bkahlert.nebula;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.bkahlert.nebula.utils.ExecUtils;

public class InformationManagerSourceProvider extends AbstractSourceProvider {

	private static final Logger LOGGER = Logger.getLogger(InformationManagerSourceProvider.class);
	
	public static final Object NULL_MANAGER = new Object();
	public static final Object NULL_CONTROL = new Object();
	public static final Object NULL_INPUT = new Object();

	private static final long timeForDetailedInformationControlToOpen = 500;
	private static long lastNotNullControlChanged = 0;
	private static long lastNotNullInputChanged = 0;

	private static final List<InformationManagerSourceProvider> INSTANCES = new ArrayList<InformationManagerSourceProvider>();

	public static void managerChanged(Object manager) {
		if (manager == null) {
			manager = NULL_MANAGER;
		}
		for (InformationManagerSourceProvider informationManagerSourceProvider : INSTANCES) {
			informationManagerSourceProvider.fireManagerChanged(manager);
		}
	}

	public static void controlChanged(Object control) {
		if (control == null) {
			control = NULL_CONTROL;
		}
		long now = new Date().getTime();
		if (control != NULL_CONTROL
				|| now - lastNotNullControlChanged > timeForDetailedInformationControlToOpen) {
			lastNotNullControlChanged = now;
			for (InformationManagerSourceProvider informationManagerSourceProvider : INSTANCES) {
				informationManagerSourceProvider.fireControlChanged(control);
			}
		}
	}

	public static void inputChanged(Object input) {
		if (input == null) {
			input = NULL_INPUT;
		}
		long now = new Date().getTime();
		if (input != NULL_INPUT
				|| now - lastNotNullInputChanged > timeForDetailedInformationControlToOpen) {
			lastNotNullInputChanged = now;
			for (InformationManagerSourceProvider informationManagerSourceProvider : INSTANCES) {
				informationManagerSourceProvider.fireInputChanged(input);
			}
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

	protected Object manager = NULL_MANAGER;
	protected Object control = NULL_CONTROL;
	protected Object input = NULL_INPUT;

	public InformationManagerSourceProvider() {
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
		this.manager = manager;
		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					InformationManagerSourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
							MANAGER, manager);
				}
			});
		} catch (Exception e) {
			LOGGER.fatal("Error firing systemClipboardTransferable changed event", e);
			throw (new RuntimeException(e));
		}
	}

	private final void fireControlChanged(final Object control) {
		this.control = control;
		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					InformationManagerSourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
							CONTROL, control);
				}
			});
		} catch (Exception e) {
			LOGGER.fatal("Error firing systemClipboardTransferable changed event", e);
			throw (new RuntimeException(e));
		}
	}

	private final void fireInputChanged(final Object input) {
		this.input = input;
		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					InformationManagerSourceProvider.this.fireSourceChanged(ISources.WORKBENCH,
							INPUT, input);
				}
			});
		} catch (Exception e) {
			LOGGER.fatal("Error firing systemClipboardTransferable changed event", e);
			throw (new RuntimeException(e));
		}
	}
}
