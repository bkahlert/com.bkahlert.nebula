package com.bkahlert.nebula.screenshots.impl.webpage;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class WebpageBoundsFactory {

	public static final Point DEFAULT_RESOLUTION = new Point(1024, 768);

	/**
	 * @see <a href="http://resizemybrowser.com">resizemybrowser.com</a>
	 */
	public static enum Device {
		IPHONE3, IPHONE3G, IPHONE4, IPHONE5, IPAD, NETBOOK, LAPTOP15;

		public Point getResolution() {
			switch (this) {
			case IPHONE3:
			case IPHONE3G:
				return new Point(320, 480);
			case IPHONE4:
				return new Point(640, 960);
			case IPHONE5:
				return new Point(640, 1136);
			case IPAD:
				return new Point(768, 1024);
			case NETBOOK:
				return new Point(1024, 600);
			case LAPTOP15:
				return new Point(1440, 900);
			default:
				return new Point(1024, 768);
			}
		}
	}

	public static Rectangle getBounds(Device device) {
		return getBounds(device, 0, 0);
	}

	public static Rectangle getBounds(Device device, Point scrollPosition) {
		int x = scrollPosition != null ? scrollPosition.x : 0;
		int y = scrollPosition != null ? scrollPosition.y : 0;
		return getBounds(device, x, y);
	}

	public static Rectangle getBounds(Device device, int scrollX, int scrollY) {
		Point resolution = device != null ? device.getResolution()
				: DEFAULT_RESOLUTION;
		return new Rectangle(scrollX, scrollY, resolution.x, resolution.y);
	}

}
