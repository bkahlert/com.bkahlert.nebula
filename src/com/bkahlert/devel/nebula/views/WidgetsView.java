package com.bkahlert.devel.nebula.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.widgets.demo.ComposerDemo;
import com.bkahlert.devel.nebula.widgets.demo.EditorDemo;
import com.bkahlert.nebula.widgets.image.Image;
import com.bkahlert.nebula.widgets.image.Image.IImageListener;

public class WidgetsView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.WidgetsView";
	@SuppressWarnings("unused")
	private int redrawInterval = 0;

	public WidgetsView() {
	}

	@Override
	public void createPartControl(final Composite parent) {

		parent.setBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_GRAY));
		parent.setLayout(new GridLayout(1, false));

		final Image image = new Image(parent, SWT.NONE);
		image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false,
				1, 1));
		image.addImageListener(new IImageListener() {
			@Override
			public void imageLoaded(Point size) {
				System.out.println("Image loaded: " + size);
			}

			@Override
			public void imageResized(Point size) {
				System.out.println("Image resized: " + size);
			}
		});
		image.load(
				"http://static3.depositphotos.com/1006137/238/i/950/depositphotos_2389382-Old--Grunge-background.jpg",
				new Runnable() {
					@Override
					public void run() {
						parent.layout();
					}
				});

		EditorDemo editorDemo = new EditorDemo(parent, SWT.BORDER);
		editorDemo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		ComposerDemo composerDemo = new ComposerDemo(parent, SWT.BORDER);
		composerDemo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		/*
		 * Button openFileListDialog = new Button(parent, SWT.PUSH);
		 * openFileListDialog.setText("Open File List Dialog");
		 * openFileListDialog.addSelectionListener(new SelectionAdapter() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) {
		 * DirectoryListDialog dialog = new DirectoryListDialog( new Shell(),
		 * Arrays.asList(new File("abc"), new File( "/Users/bkahlert/etc.")));
		 * dialog.create(); dialog.setTitle("Data Directories");
		 * dialog.setText("Add or remove data directories."); if (dialog.open()
		 * == Window.OK) { List<File> directories = dialog.getDirectories();
		 * System.out.println(directories.size()); } } });
		 * 
		 * RoundedLabels roundedLabels = new RoundedLabels(parent, SWT.BORDER,
		 * new RGB(200, 200, 200)); roundedLabels.setTexts(new String[] { "abc",
		 * "def", "kjkdjklsdjdsdslkjlkjlk" });
		 * 
		 * RoundedLabels roundedLabels2 = new RoundedLabels(parent, SWT.NONE,
		 * new RGB(200, 200, 200)); roundedLabels2.setTexts(new String[] {
		 * "abc", "def", "kjkdjklsdjdsdslkjlkjlk" });
		 * 
		 * Composite wrapper = new Composite(parent, SWT.NONE);
		 * wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		 * wrapper.setLayout(GridLayoutFactory.swtDefaults().margins(1, 1)
		 * .spacing(2, 0).numColumns(2).create()); Label label = new
		 * Label(wrapper, SWT.NONE);
		 * label.setLayoutData(GridDataFactory.swtDefaults()
		 * .align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4).create());
		 * label.setText("Filters:"); RoundedLabels roundedLabels3 = new
		 * RoundedLabels(wrapper, SWT.NONE, new RGB(200, 200, 200));
		 * roundedLabels3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		 * true)); roundedLabels3.setTexts(new String[] { "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj" });
		 * 
		 * ITimeline timeline = new Timeline(parent, SWT.BORDER); ((Control)
		 * timeline).setLayoutData(GridDataFactory.fillDefaults() .grab(true,
		 * false).create()); ITimelineInput input = null; timeline.show(input,
		 * 500, 500, null);
		 * 
		 * List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0,
		 * 255), new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
		 * new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255, 255),
		 * new RGB(127, 127, 127), new RGB(127, 127, 255), new RGB(127, 255,
		 * 127), new RGB(127, 255, 255), new RGB(255, 127, 127), new RGB(255,
		 * 127, 255), new RGB(255, 255, 127), new RGB(232, 232, 232)); Composite
		 * colorComposite = new Composite(parent, SWT.BORDER);
		 * colorComposite.setLayoutData
		 * (GridDataFactory.fillDefaults().create());
		 * colorComposite.setLayout(new GridLayout(colors.size(), true)); for
		 * (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.2f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.1f).toClassicRGB()); } for (RGB rgb : colors) { new
		 * ColorPicker(colorComposite, rgb); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.0f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.9f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.8f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.7f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.2f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.1f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.0f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 1.0f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.9f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.8f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.7f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.2f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.1f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.0f).toClassicRGB()); }
		 * 
		 * if (redrawInterval > 0) { new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { Thread.sleep(redrawInterval); }
		 * catch (InterruptedException e) {
		 * 
		 * } Display.getDefault().syncExec(new Runnable() {
		 * 
		 * @Override public void run() { for (Control c : parent.getChildren())
		 * { if (c != null && !parent.isDisposed()) c.dispose(); }
		 * createPartControl(parent); parent.layout(); } }); } }).start(); } /*
		 */
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
