package com.bkahlert.devel.nebula.views;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.dialogs.DirectoryListDialog;
import com.bkahlert.devel.nebula.widgets.ColorPicker;
import com.bkahlert.devel.nebula.widgets.RoundedLabels;
import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

public class WidgetsView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.WidgetsView";
	private int redrawInterval = 0;

	public WidgetsView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(GridLayoutFactory.swtDefaults().create());

		new Thread(new Runnable() {
			@Override
			public void run() {
				Job x = new Job("Parallel Job Execution") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						final SubMonitor subMonitor = SubMonitor
								.convert(monitor);
						subMonitor.beginTask("Begin Task", 100);
						subMonitor.setWorkRemaining(100);
						Thread a = new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(100);
										System.err.println("a - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									subMonitor.worked(1);
								}
							}
						});
						Thread b = new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(150);
										System.err.println("b - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									subMonitor.worked(1);
								}
							}
						});
						a.start();
						b.start();
						try {
							a.join();
							System.err.println("a finished");
							b.join();
							System.err.println("b finished");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						subMonitor.done();
						return Status.OK_STATUS;
					}
				};
				x.schedule();
				try {
					x.join();
					System.err.println("x joined");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Job y = new Job("Parallel Job Execution with Sub Monitors") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						final SubMonitor subMonitor = SubMonitor
								.convert(monitor);
						subMonitor.beginTask("Begin Task", 2);
						Thread a = new Thread(new Runnable() {
							@Override
							public void run() {
								SubMonitor aMonitor = subMonitor.newChild(1);
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(100);
										System.err.println("sub a - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									aMonitor.worked(1);
								}
								aMonitor.done();
							}
						});
						Thread b = new Thread(new Runnable() {
							@Override
							public void run() {
								SubMonitor bMonitor = subMonitor.newChild(1);
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(150);
										System.err.println("sub b - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									bMonitor.worked(1);
								}
								bMonitor.done();
							}
						});
						a.start();
						b.start();
						try {
							a.join();
							System.err.println("sub a finished");
							b.join();
							System.err.println("sub b finished");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						subMonitor.done();
						return Status.OK_STATUS;
					}
				};
				y.schedule();
				try {
					y.join();
					System.err.println("y joined");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}).start();

		Button openFileListDialog = new Button(parent, SWT.PUSH);
		openFileListDialog.setText("Open File List Dialog");
		openFileListDialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryListDialog dialog = new DirectoryListDialog(
						new Shell(), Arrays.asList(new File("abc"), new File(
								"/Users/bkahlert/etc.")));
				dialog.create();
				dialog.setTitle("Data Directories");
				dialog.setText("Add or remove data directories.");
				if (dialog.open() == Window.OK) {
					List<File> directories = dialog.getDirectories();
					System.out.println(directories.size());
				}
			}
		});

		RoundedLabels roundedLabels = new RoundedLabels(parent, SWT.BORDER,
				new RGB(200, 200, 200));
		roundedLabels.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk" });

		RoundedLabels roundedLabels2 = new RoundedLabels(parent, SWT.NONE,
				new RGB(200, 200, 200));
		roundedLabels2.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk" });

		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		wrapper.setLayout(GridLayoutFactory.swtDefaults().margins(1, 1)
				.spacing(2, 0).numColumns(2).create());
		Label label = new Label(wrapper, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4).create());
		label.setText("Filters:");
		RoundedLabels roundedLabels3 = new RoundedLabels(wrapper, SWT.NONE,
				new RGB(200, 200, 200));
		roundedLabels3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		roundedLabels3.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj" });

		Timeline timeline = new Timeline(parent, SWT.BORDER);
		timeline.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		String json = "{\"events\":[{\"title\":\"READY - http://www.seqan.de/dddoc/html_devel/INDEX_Function_Motif+_Search.html#absFreqOfLettersInSeq\",\"start\":\"Sep 13 2011 14:05:22 GMT+0200\",\"end\":\"Sep 13 2011 14:05:31 GMT+0200\",\"durationEvent\":true,\"icon\":\"http://www.freebase.com/api/trans/image_thumb//wikipedia/images/en_id/2462532?maxheight=76&amp;mode=fillcrop&amp;maxwidth=76\",\"image\":\"file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.doclog/bin/data/screenshots/http%253A%252F%252Fwww.seqan.de%252Fdddoc%252Fhtml_devel%252FINDEX_Function_Motif%252B_Search.html%2523absFreqOfLettersInSeq-179,365-0,0.png\"},{\"title\":\"SCROLL - http://www.seqan.de/dddoc/html_devel/FUNCTION.abs_Freq_Of_Letters_In_Seq.html\",\"start\":\"Sep 13 2011 14:05:32 GMT+0200\",\"end\":\"Sep 13 2011 14:07:51 GMT+0200\",\"durationEvent\":true,\"icon\":\"http://www.freebase.com/api/trans/image_thumb//wikipedia/images/en_id/2462532?maxheight=76&amp;mode=fillcrop&amp;maxwidth=76\",\"image\":\"file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.doclog/bin/data/screenshots/http%253A%252F%252Fwww.seqan.de%252Fdddoc%252Fhtml_devel%252FFUNCTION.abs_Freq_Of_Letters_In_Seq.html-1137,520-0,77.png\"},{\"title\":\"UNLOAD - http://www.seqan.de/dddoc/html_devel/FUNCTION.abs_Freq_Of_Letters_In_Seq.html\",\"start\":\"Sep 13 2011 14:07:52 GMT+0200\",\"end\":\"Sep 13 2011 14:07:52 GMT+0200\",\"durationEvent\":true,\"icon\":\"http://www.freebase.com/api/trans/image_thumb//wikipedia/images/en_id/2462532?maxheight=76&amp;mode=fillcrop&amp;maxwidth=76\",\"image\":\"file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.doclog/bin/data/screenshots/http%253A%252F%252Fwww.seqan.de%252Fdddoc%252Fhtml_devel%252FFUNCTION.abs_Freq_Of_Letters_In_Seq.html-1137,520-0,77.png\"}]}";
		timeline.show(json);

		List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0, 255),
				new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
				new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255,
						255), new RGB(127, 127, 127), new RGB(127, 127, 255),
				new RGB(127, 255, 127), new RGB(127, 255, 255), new RGB(255,
						127, 127), new RGB(255, 127, 255), new RGB(255, 255,
						127), new RGB(232, 232, 232));
		Composite colorComposite = new Composite(parent, SWT.BORDER);
		colorComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		colorComposite.setLayout(new GridLayout(colors.size(), true));
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.2f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.1f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			new ColorPicker(colorComposite, rgb);
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.0f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.9f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.8f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.7f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.2f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.1f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.0f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					1.0f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.9f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.8f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.7f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.2f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.1f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.devel.nebula.colors.RGB rgbx = new com.bkahlert.devel.nebula.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.0f).toClassicRGB());
		}

		if (redrawInterval > 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(redrawInterval);
					} catch (InterruptedException e) {

					}
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							for (Control c : parent.getChildren()) {
								if (c != null && !parent.isDisposed())
									c.dispose();
							}
							createPartControl(parent);
							parent.layout();
						}
					});
				}
			}).start();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
