package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract base class for the Yune Views to simplify creating UI Components.
 * 
 * For usage with Components that can't extend it (f.e. the MainView) the
 * methods are static aswell.
 */
public abstract class AbstractYuneView extends Composite {

	protected AbstractYuneView(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Create a container with the given attributes
	 */
	protected static Group createContainer(Composite parent) {
		return new Group(parent, SWT.NONE);
	}

	/**
	 * Create a container with the given attributes
	 */
	protected static Group createContainer(Composite parent, String text) {
		Group g = new Group(parent, SWT.NONE);
		g.setText(text);
		return g;
	}

	/**
	 * Create a container with the given attributes
	 */
	protected static Group createContainer(Composite parent, String text, String toolTip) {
		Group g = new Group(parent, SWT.NONE);
		g.setText(text);
		g.setToolTipText(toolTip);
		return g;
	}

	/**
	 * Create a Button with the given attributes
	 */
	protected static Button createButton(Composite parent, int style, String text, String toolTip) {
		return createButton(parent, style, text, toolTip, true, false);
	}

	/**
	 * Create a Button with the given attributes
	 */
	protected static Button createButton(Composite parent, int style, String text, String toolTip, boolean enabled,
			boolean selection) {
		Button b = new Button(parent, style);
		b.setText(text);
		b.setToolTipText(toolTip);
		b.setEnabled(enabled);
		b.setSelection(selection);
		return b;
	}

	protected static Spinner createSpinner(Composite parent, int[] v, boolean enabled) {
		return createSpinner(parent, v, null, enabled);
	}

	protected static Spinner createSpinner(Composite parent, boolean enabled) {
		return createSpinner(parent, new int[] { 5, 1, 20, 0, 1, 5 }, null, enabled);
	}

	protected static Spinner createSpinner(Composite parent, String toolTip, boolean enabled) {
		return createSpinner(parent, new int[] { 5, 1, 20, 0, 1, 5 }, toolTip, enabled);
	}

	/**
	 * Creates a Spinner with the given arguments
	 * 
	 * @param v Spinner values {int selection, int minimum, int maximum, int digits,
	 *          int increment, int pageIncrement}
	 */
	protected static Spinner createSpinner(Composite parent, int[] v, String toolTip, boolean enabled) {
		return createSpinner(parent, SWT.None, v, toolTip, enabled);
	}

	/**
	 * Creates a Spinner with the given arguments
	 * 
	 * @param v Spinner values {int selection, int minimum, int maximum, int digits,
	 *          int increment, int pageIncrement}
	 */
	protected static Spinner createSpinner(Composite parent, int style, int[] v, String toolTip, boolean enabled) {
		assert (v.length == 6);
		Spinner s = new Spinner(parent, style);
		s.setValues(v[0], v[1], v[2], v[3], v[4], v[5]);
		s.setToolTipText(toolTip);
		s.setEnabled(enabled);
		return s;
	}

	/**
	 * Create a Label with the given attributes
	 */
	protected static Label createLabel(Composite parent, String text) {
		return createLabel(parent, SWT.NONE, text, true);
	}

	/**
	 * Create a Label with the given attributes
	 */
	protected static Label createLabel(Composite parent, int style, String text) {
		return createLabel(parent, style, text, true);
	}

	/**
	 * Create a Label with the given attributes
	 */
	protected static Label createLabel(Composite parent, int style, String text, boolean visible) {
		Label l = new Label(parent, style);
		l.setText(text);
		l.setVisible(visible);
		return l;
	}

	/**
	 * Create a Text with the given attributes
	 */
	protected static Text createText(Composite parent, int style) {
		return new Text(parent, style);
	}

	/**
	 * Create a Text with the given attributes
	 */
	protected static Text createText(Composite parent, int style, String text, String toolTip, boolean visible) {
		Text t = new Text(parent, style);
		t.setText(text);
		t.setToolTipText(toolTip);
		t.setVisible(visible);
		return t;
	}

	/**
	 * Sets the form layout for the given container with the given Margins,
	 * defaulting to 5 in each direction
	 */
	protected static void setGroupMargins(Composite container) {
		setGroupMargins(container, new Integer[] { 5, 5, 5, 5 });
	}

	/**
	 * Sets the form layout for the given container with the given Margins.
	 */
	protected static void setGroupMargins(Composite container, Integer[] m) {
		FormLayout groupLayout = new FormLayout();
		if(m.length >0)
			groupLayout.marginRight = m[0];
		if(m.length >1)
			groupLayout.marginTop = m[1];
		if(m.length >2)
			groupLayout.marginBottom = m[2];
		if(m.length >3)
			groupLayout.marginLeft = m[3];
		container.setLayout(groupLayout);
	}

	/**
	 * Set the Layout data for the given Item.
	 * 
	 * This always can get up to 4 parameters and any unused attachments are
	 * automatically filled with nulls. Arguments must be in order:
	 * <ol>
	 * <li>Top</li>
	 * <li>Left</li>
	 * <li>Right</li>
	 * <li>Bottom</li>
	 * </ol>
	 * 
	 * If you want to skip f.e. setting Top, you NEED to fill it with null manually.
	 */
	protected static void layout(Control item, FormAttachment... a) {
		FormData data = new FormData();
		data.top  = a.length > 0 ? a[0] : null;
		data.left = a.length > 1 ? a[1] : null;
		data.right = a.length > 2 ? a[2] : null;
		data.bottom = a.length > 3 ? a[3] : null;
		item.setLayoutData(data);
	}

	/**
	 * Set the Layout data for the given Item.
	 * 
	 * This always can get up to 4 parameters and any unused attachments are
	 * automatically filled with nulls. Arguments must be in order:
	 * <ol>
	 * <li>Top</li>
	 * <li>Left</li>
	 * <li>Right</li>
	 * <li>Bottom</li>
	 * </ol>
	 * 
	 * If you want to skip f.e. setting Top, you NEED to fill it with null manually.
	 */
	protected static void layout(Control item, int width, int height, FormAttachment... a) {
		FormData data = new FormData();
		data.top  = a.length > 0 ? a[0] : null;
		data.left = a.length > 1 ? a[1] : null;
		data.right = a.length > 2 ? a[2] : null;
		data.bottom = a.length > 3 ? a[3] : null;
		if(width != 0)
			data.width = width;
		if(height != 0)
			data.height = height;
		item.setLayoutData(data);
	}

	/**
	 * Set the Layout data for the given Item, if one of the direction parameters is
	 * null it will not be set.
	 */
	protected static void defaultLayout(Control item) {
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(0, 0);
		buttonData.left = new FormAttachment(0, 0);
		item.setLayoutData(buttonData);
	}

}