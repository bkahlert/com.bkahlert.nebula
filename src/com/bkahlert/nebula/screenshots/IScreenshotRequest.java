package com.bkahlert.nebula.screenshots;

/**
 * Implementations can be submitted to an {@link IScreenshotTaker}.
 * 
 * @author bkahlert
 */
public interface IScreenshotRequest {

	public enum FORMAT {
		PNG, JPEG, GIF;

		public String getName() {
			switch (this) {
			case JPEG:
				return "jpg";
			}
			return this.toString().toLowerCase();
		}
	}

	/**
	 * Returns which format the screenshot should have.
	 * 
	 * @return
	 */
	public FORMAT getFormat();

	public IScreenshotCustomizer getCustomizer();

}
