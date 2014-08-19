package com.bkahlert.nebula.utils;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlay.Quadrant;

public class DecorationOverlayIcon extends CompositeImageDescriptor {

	private static final Logger LOGGER = Logger
			.getLogger(DecorationOverlayIcon.class);

	public static interface ImageOverlay {
		public static enum Quadrant {
			TopLeft, TopRight, BottomRight, BottomLeft, Underlay, Replace;
		}

		public Quadrant getQuadrant();

		public ImageDescriptor getImageDescriptor();
	}

	public static class ImageOverlayImpl implements ImageOverlay {

		private final ImageDescriptor imageDescriptor;
		private final Quadrant quadrant;

		public ImageOverlayImpl(ImageDescriptor imageDescriptor,
				Quadrant quadrant) {
			this.imageDescriptor = imageDescriptor;
			this.quadrant = quadrant;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return this.imageDescriptor;
		}

		@Override
		public Quadrant getQuadrant() {
			return this.quadrant;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((this.imageDescriptor == null) ? 0
							: this.imageDescriptor.hashCode());
			result = prime * result
					+ ((this.quadrant == null) ? 0 : this.quadrant.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			ImageOverlayImpl other = (ImageOverlayImpl) obj;
			if (this.imageDescriptor == null) {
				if (other.imageDescriptor != null) {
					return false;
				}
			} else if (!this.imageDescriptor.equals(other.imageDescriptor)) {
				return false;
			}
			if (this.quadrant != other.quadrant) {
				return false;
			}
			return true;
		}

	}

	// the base image
	private final ImageData base;

	// the overlay images
	private final ImageOverlay[] overlays;

	private final Point size;

	/**
	 * Create the decoration overlay for the base image using the given
	 * {@link ImageOverlay}.
	 * 
	 * @param baseImage
	 *            the base image
	 * @param overlaysArray
	 *            the overlay images
	 * @param sizeValue
	 *            the size of the resulting image
	 */
	public DecorationOverlayIcon(ImageData baseImage,
			ImageOverlay... overlaysArray) {
		this.base = baseImage;
		this.overlays = overlaysArray;
		this.size = new Point(baseImage.width, baseImage.height);
	}

	/**
	 * Draw the overlays for the receiver.
	 * 
	 * @param overlays2
	 */
	private void drawOverlays(ImageOverlay[] overlays) {
		for (ImageOverlay overlay : overlays) {
			if (overlay == null) {
				continue;
			}
			ImageData overlayData = overlay.getImageDescriptor().getImageData();
			// Use the missing descriptor if it is not there.
			if (overlayData == null) {
				overlayData = ImageDescriptor.getMissingImageDescriptor()
						.getImageData();
			}
			switch (overlay.getQuadrant()) {
			case TopLeft:
				this.drawImage(overlayData, 0, 0);
				break;
			case TopRight:
				this.drawImage(overlayData, this.size.x - overlayData.width, 0);
				break;
			case BottomRight:
				this.drawImage(overlayData, this.size.x - overlayData.width,
						this.size.y - overlayData.height);
				break;
			case BottomLeft:
				this.drawImage(overlayData, 0, this.size.y - overlayData.height);
				break;
			case Underlay:
				break;
			default:
				LOGGER.error("Unknown " + Quadrant.class.getSimpleName() + ": "
						+ overlay.getQuadrant());
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DecorationOverlayIcon)) {
			return false;
		}
		DecorationOverlayIcon other = (DecorationOverlayIcon) o;
		return this.base.equals(other.base)
				&& Arrays.equals(this.overlays, other.overlays);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int code = System.identityHashCode(this.base);
		for (int i = 0; i < this.overlays.length; i++) {
			if (this.overlays[i] != null) {
				code ^= this.overlays[i].hashCode();
			}
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage
	 * (int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		for (ImageOverlay overlay : this.overlays) {
			if (overlay.getQuadrant() == Quadrant.Underlay) {
				ImageDescriptor underlay = overlay.getImageDescriptor();
				if (underlay != null) {
					this.drawImage(underlay.getImageData(), 0, 0);
				}
			}
		}
		boolean replaced = false;
		for (ImageOverlay overlay : this.overlays) {
			if (overlay.getQuadrant() == Quadrant.Replace) {
				if (replaced) {
					LOGGER.error("An image has already been replaced");
					break;
				}
				this.drawImage(overlay.getImageDescriptor().getImageData(), 0,
						0);
				replaced = true;
			}
		}
		if (!replaced) {
			this.drawImage(this.base, 0, 0);
		}
		this.drawOverlays(this.overlays);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return this.size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.resource.CompositeImageDescriptor#getTransparentPixel()
	 */
	@Override
	protected int getTransparentPixel() {
		return this.base.transparentPixel;
	}

}
