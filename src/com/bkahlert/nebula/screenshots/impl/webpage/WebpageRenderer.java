package com.bkahlert.nebula.screenshots.impl.webpage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.JQueryEnabledBrowserComposite;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRenderer;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;
import com.bkahlert.nebula.utils.HttpUtils;
import com.bkahlert.nebula.utils.ShellUtils;

public class WebpageRenderer implements
		IWebpageScreenshotRenderer<IWebpageScreenshotRequest> {

	public static class WebpageRendererException extends Exception {

		private static final long serialVersionUID = 1L;

		public WebpageRendererException() {
			super();
		}

		public WebpageRendererException(String message, Throwable cause) {
			super(message, cause);
		}

		public WebpageRendererException(String message) {
			super(message);
		}

		public WebpageRendererException(Throwable cause) {
			super(cause);
		}

	}

	private static final Logger LOGGER = Logger
			.getLogger(WebpageRenderer.class);

	private static class Renderer extends Dialog {

		private JQueryEnabledBrowserComposite browser;

		public Renderer(Shell parentShell) {
			super(parentShell);
		}

		private void resize(Point innerDimensions) {
			Rectangle trim = this.getShell().computeTrim(0, 0,
					innerDimensions.x, innerDimensions.y);
			this.getShell().setSize(trim.width, trim.height);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new FillLayout());

			this.browser = new JQueryEnabledBrowserComposite(composite,
					SWT.NONE);

			return composite;
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			return parent;
		}

	}

	private Shell parentShell;
	private Map<Renderer, IWebpageScreenshotRequest> renderers;
	private List<Renderer> renderersInUse;
	private List<Renderer> availableRenderers;

	public WebpageRenderer(Shell parentShell) {
		this.parentShell = parentShell;
		this.renderers = new HashMap<Renderer, IWebpageScreenshotRequest>();
		this.renderersInUse = new ArrayList<Renderer>();
		this.availableRenderers = new ArrayList<Renderer>();
	}

	synchronized private Renderer getRenderer(IWebpageScreenshotRequest request) {
		if (this.availableRenderers.size() == 0) {
			Shell shell = this.parentShell != null ? new Shell(this.parentShell)
					: new Shell(Display.getCurrent());
			Renderer renderer = new Renderer(shell);
			this.availableRenderers.add(renderer);
		}
		Renderer rendererInUse = this.availableRenderers.remove(0);
		this.renderers.put(rendererInUse, request);
		this.renderersInUse.add(rendererInUse);
		return rendererInUse;
	}

	synchronized private void releaseRenderer(Renderer renderer) {
		Assert.isLegal(renderer != null);
		this.renderers.put(renderer, null);
		this.renderersInUse.remove(renderer);
		this.availableRenderers.add(renderer);
	}

	@Override
	public Callable<IScreenshotRendererSession> render(
			final IWebpageScreenshotRequest request) {
		return new Callable<IScreenshotRendererSession>() {

			private Renderer renderer = null;

			@Override
			public IScreenshotRendererSession call() throws Exception {
				try {
					LOGGER.info("Rendering " + request.getUri().toString());

					this.verifiyResponseCode();

					this.configuredRenderer();

					if (!this.loadUri()) {
						throw new WebpageRendererException("Opening "
								+ request.getUri() + " timeout out");
					}

					if (request.getCustomizer() != null) {
						request.getCustomizer().betweenLoadingAndScrolling(
								request, WebpageRenderer.this);
					}

					if (this.scroll()) {
						Thread.sleep(800);
					}

					return new IScreenshotRendererSession() {
						@Override
						public void bringToFront() {
							ExecutorUtil.syncExec(new Runnable() {
								@Override
								public void run() {
									try {
										for (Renderer other : WebpageRenderer.this.renderers
												.keySet()) {
											ShellUtils.setVisible(
													other.getShell(),
													other == renderer);
										}
										renderer.getShell().setActive();
										renderer.getShell().forceActive();
										renderer.getShell().forceFocus();
									} catch (Exception e) {
										LOGGER.error(e);
									}
								}
							});
						}

						@Override
						public Rectangle getBounds() {
							return ShellUtils.getInnerArea(renderer.getShell());
						}

						@Override
						public void dispose() {
							WebpageRenderer.this.releaseRenderer(renderer);
						}
					};
				} catch (Exception e) {
					if (this.renderer != null) {
						WebpageRenderer.this.releaseRenderer(this.renderer);
					}
					throw e;
				}
			}

			private void verifiyResponseCode() throws WebpageRendererException {
				try {
					int responseCode = HttpUtils.getResponseCode(request
							.getUri());
					if (responseCode < 200 || responseCode >= 300) {
						throw new WebpageRendererException(
								"Unsupported response code " + responseCode
										+ " returned for " + request.getUri());
					}
				} catch (MalformedURLException e) {
					throw new WebpageRendererException(e);
				} catch (IOException e) {
					throw new WebpageRendererException(e);
				}
			}

			private void configuredRenderer() {
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						renderer = WebpageRenderer.this.getRenderer(request);
						renderer.setBlockOnOpen(false);
						renderer.open();
						renderer.resize(request.getDimensions());
					}
				});
			}

			private boolean loadUri() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser.open(request.getUri(),
						request.getTimeout()).get();
			}

			private boolean scroll() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser.scrollTo(
						request.getScrollPosition()).get();
			}

		};
	}

	@Override
	public IJQueryEnabledBrowserComposite getBrowser(
			IWebpageScreenshotRequest request) {
		for (Entry<Renderer, IWebpageScreenshotRequest> entry : WebpageRenderer.this.renderers
				.entrySet()) {
			if (entry.getValue() == request) {
				return entry.getKey().browser;
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				for (Iterator<Renderer> iterator = WebpageRenderer.this.renderers
						.keySet().iterator(); iterator.hasNext();) {
					Renderer renderer = iterator.next();
					renderer.close();
					iterator.remove();
					WebpageRenderer.this.availableRenderers.remove(renderer);
					WebpageRenderer.this.renderersInUse.remove(renderer);
				}
			}
		});
	}

}
