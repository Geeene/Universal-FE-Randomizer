package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import ui.model.FE9OtherCharacterOptions;

public class CONAffinityView extends AbstractYuneView {

	private Group container;

	private Button randomizeCONButton;
	private Label conVarianceLabel;
	private Spinner conVarianceSpinner;

	private Button randomizeAffinityButton;

	public CONAffinityView(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		container = createContainer(this, "Other Character Settings");
		setGroupMargins(container);

		randomizeCONButton = createButton(container, SWT.CHECK, "Randomize Constitution",
				"Randomizes Constitution, which affects weight, and therefore, the ability to\nto shove/rescue and to be shoved/rescued.");
		defaultLayout(randomizeCONButton);

		conVarianceLabel = createLabel(container, SWT.NONE, "Variance:");
		layout(conVarianceLabel, new FormAttachment(conVarianceSpinner, 0, SWT.CENTER),
				new FormAttachment(conVarianceSpinner, -5));

		conVarianceSpinner = createSpinner(container, new int[] { 3, 1, 8, 0, 1, 1 },
				"Determines how far in each direction Constitution is allowed to adjust.", false);
		layout(conVarianceSpinner, new FormAttachment(randomizeCONButton, 5), null, new FormAttachment(100, -12));

		randomizeCONButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				conVarianceLabel.setEnabled(randomizeCONButton.getSelection());
				conVarianceSpinner.setEnabled(randomizeCONButton.getSelection());
			}
		});

		randomizeAffinityButton = createButton(container, SWT.CHECK, "Randomize Affinity",
				"Randomizes affinity, which affects support bonuses.");
		layout(randomizeAffinityButton, new FormAttachment(conVarianceSpinner, 5),
				new FormAttachment(randomizeCONButton, 0, SWT.LEFT));
	}

	public FE9OtherCharacterOptions getOtherCharacterOptions() {
		return new FE9OtherCharacterOptions(randomizeCONButton.getSelection(), conVarianceSpinner.getSelection(),
				randomizeAffinityButton.getSelection());
	}

	public void setOtherCharacterOptions(FE9OtherCharacterOptions options) {
		randomizeCONButton.setSelection(options.randomizeCON);
		conVarianceLabel.setEnabled(options.randomizeCON);
		conVarianceSpinner.setEnabled(options.randomizeCON);
		conVarianceSpinner.setSelection(options.conVariance);

		randomizeAffinityButton.setSelection(options.randomizeAffinity);
	}
}
