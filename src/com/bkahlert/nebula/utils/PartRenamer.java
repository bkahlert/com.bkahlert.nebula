package com.bkahlert.nebula.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;

public class PartRenamer<T> {

	private static final Logger LOGGER = Logger.getLogger(PartRenamer.class);

	public static void setTitle(final ViewPart part, final String title) {
		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Method m = ViewPart.class.getDeclaredMethod("setPartName",
							String.class);
					m.setAccessible(true);
					m.invoke(part, title);
					m.setAccessible(false);
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		});

	}

	public static void setImage(final WorkbenchPart part, final Image image) {
		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Method m = WorkbenchPart.class.getDeclaredMethod(
							"setTitleImage", Image.class);
					m.setAccessible(true);
					Image copy = image != null ? new Image(
							Display.getCurrent(), image, SWT.IMAGE_COPY) : null;
					m.invoke(part, copy);
					m.setAccessible(false);
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		});
	}

	public static void setInfo(final ViewPart part, final String title,
			final Image image) {
		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				setTitle(part, title);
				setImage(part, image);
			}
		});
	}

	public static final Image ERROR = PlatformUI.getWorkbench()
			.getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

	private final ViewPart part;
	private final IConverter<T, Pair<String, Image>> converter;
	private String title;
	private Image image;

	public PartRenamer(final ViewPart part,
			IConverter<T, Pair<String, Image>> converter) {
		Assert.isNotNull(part);
		Assert.isNotNull(converter);
		this.part = part;
		this.converter = converter;
		this.title = null;
		this.image = null;
	}

	public void apply(List<T> objects) {
		this.backup();
		if (objects == null || objects.size() == 0) {
			this.reset();
			return;
		}

		List<String> titles = new ArrayList<String>(objects.size());
		List<Image> images = new ArrayList<Image>(objects.size());
		for (T object : objects) {
			Pair<String, Image> partInfo = this.converter.convert(object);
			if (partInfo != null) {
				titles.add(partInfo.getFirst());
				images.add(partInfo.getSecond());
			} else {
				LOGGER.error("part info must no be null for " + object);
			}
		}

		String title = this.title + ": " + StringUtils.join(titles, "  |  ");
		Image image = images.get(0);
		for (int i = 1, m = images.size(); i < m; i++) {
			if (image == ERROR) {
				continue;
			}
			if (image != images.get(0)) {
				image = PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_TOOL_COPY);
				break;
			}
		}
		setInfo(this.part, title, image);
	}

	public void apply(T object) {
		this.apply(object != null ? Arrays.asList(object) : null);
	}

	private void backup() {
		if (this.title == null) {
			this.title = this.part.getPartName();
			this.image = this.part.getTitleImage();
			this.image = this.image != null ? new Image(Display.getCurrent(),
					this.image, SWT.IMAGE_COPY) : null;
		}
	}

	public void reset() {
		this.backup();
		setInfo(this.part, this.title, this.image);
	}
}