package com.bkahlert.devel.nebula.widgets.editor;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;

/**
 * In contrast to the classic {@link Editor} instances of this class
 * automatically save changes whenever they are made or a new object is loaded.
 * 
 * @author bkahlert
 * 
 * @param <T>
 */
public abstract class AutosaveEditor<T> extends Editor<T> {

	public AutosaveEditor(Composite parent, int style,
			long delayChangeEventUpTo, ToolbarSet toolbarSet) {
		super(parent, style, delayChangeEventUpTo, toolbarSet);

		this.composer.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				AutosaveEditor.this.save((String) e.data);
			}
		});
	}

}
