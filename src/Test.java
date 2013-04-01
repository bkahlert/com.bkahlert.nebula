import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class Test extends Composite {

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Test(Composite parent, int style) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.margins(0, 0).spacing(0, 0).create());

		Composite leftBar = new Composite(this, SWT.NONE);
		leftBar.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_LIST_SELECTION));
		leftBar.setLayoutData(GridDataFactory.fillDefaults()
				.grab(false, true).hint(10, SWT.DEFAULT).create());

		Label separator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
		separator.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		Composite rightArea = new Composite(this, SWT.NONE);
		rightArea.setLayoutData(GridDataFactory.fillDefaults()
				.grab(false, true).hint(1, SWT.DEFAULT).create());

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
