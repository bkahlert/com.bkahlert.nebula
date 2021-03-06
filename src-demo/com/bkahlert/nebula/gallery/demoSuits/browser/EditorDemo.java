package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.about.AboutAction;

import com.bkahlert.nebula.dialogs.PopupDialog;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.editor.AutosaveEditor;
import com.bkahlert.nebula.widgets.editor.Editor;

@SuppressWarnings("restriction")
@Demo
public class EditorDemo extends AbstractDemo {

	private Editor<String> editor;

	@Override
	public void createDemo(Composite composite) {
		composite.setLayout(new FillLayout());

		this.editor = new AutosaveEditor<String>(composite, SWT.NONE, 5000,
				ToolbarSet.DEFAULT) {

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
			public void setHtml(String objectToLoad, String html,
					IProgressMonitor monitor) {
				AbstractDemo.log("saved: " + html);
				System.out.println("saved: " + html);
			}
		};
		this.editor.addAnkerListener(new IAnkerListener() {
			private PopupDialog popup = null;

			@Override
			public void ankerClicked(IAnker anker) {
				log("clicked on " + anker.getHref());
			}

			@Override
			public void ankerHovered(final IAnker anker, boolean entered) {
				log((entered ? "entered " : "left") + ": " + anker.getHref());
				if (this.popup != null) {
					this.popup.close();
					this.popup = null;
				}
				if (entered) {
					IllustratedText illustratedText = new IllustratedText(
							SWT.ICON_INFORMATION, anker.getContent());
					this.popup = new PopupDialog(illustratedText, "Test") {
						@Override
						protected Control createControls(Composite parent) {
							Label label = new Label(parent, SWT.NONE);
							label.setText(anker.toHtml());
							return label;
						};
					};
				}
			}
		});

		InformationControlManager<Editor<?>, IAnker> editorInformationControlManager = new InformationControlManager<Editor<?>, IAnker>(
				IAnker.class, new InformationControlCreator<IAnker>() {
					@Override
					protected InformationControl<IAnker> doCreateInformationControl(
							Shell parent) {
						return new EnhanceableInformationControl<IAnker, Delegate<IAnker>>(
								EditorDemo.class.getClassLoader(),
								IAnker.class, parent,
								() -> new Delegate<IAnker>() {
									private Label label;

									@Override
									public Composite build(Composite parent) {
										this.label = new Label(parent,
												SWT.BORDER);
										return parent;
									}

									@Override
									public boolean load(IAnker anker,
											ToolBarManager toolBarManager) {
										if (anker == null) {
											return false;
										}

										AbstractDemo.log(this.label.hashCode()
												+ "");

										if (toolBarManager != null) {
											toolBarManager
													.add(new AboutAction(
															PlatformUI
																	.getWorkbench()
																	.getActiveWorkbenchWindow()));
										}
										String content = toolBarManager != null ? anker
												.toHtml() : anker.getContent();
										AbstractDemo.log(content);
										this.label.setText(content);
										return true;
									}
								});
					}
				}, new ISubjectInformationProvider<Editor<?>, IAnker>() {
					private IAnker hoveredAnker = null;
					private final IAnkerListener ankerListener = new AnkerAdapter() {
						@Override
						public void ankerHovered(IAnker anker, boolean entered) {
							hoveredAnker = entered ? anker : null;
						}
					};

					@Override
					public void register(Editor<?> editor) {
						editor.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Editor<?> editor) {
						editor.removeAnkerListener(this.ankerListener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(50, 20);
					}

					@Override
					public IAnker getInformation() {
						return this.hoveredAnker;
					}
				});
		editorInformationControlManager.install(this.editor);

		this.editor.load("This is an auto-saving editor");
	}

	@Override
	public void createControls(Composite composite) {
		Button composerSetSource = new Button(composite, SWT.PUSH);
		composerSetSource.setText("Set Source 1");
		Button composerSetSource2 = new Button(composite, SWT.PUSH);
		composerSetSource2.setText("Set Source 2");
		Button composerSetNoSource = new Button(composite, SWT.PUSH);
		composerSetNoSource.setText("Set No Source");

		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditorDemo.this.editor
						.load("<p title='test'><b>Hallo</b><i>Welt 1!</i></p>");
			}
		});
		composerSetSource2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditorDemo.this.editor
						.load("<p title='test'><b>Hallo</b><i>Welt 2!</i></p>");
			}
		});
		composerSetNoSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditorDemo.this.editor.load(null);
			}
		});
	}

}
