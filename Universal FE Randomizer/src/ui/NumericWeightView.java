package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class NumericWeightView extends AbstractYuneView {

	private Button enableButton;
	private Spinner weightInput;
	private Text effectiveWeight;

	private NumericWeightViewListener listener;

	private int cachedValue;

	public interface NumericWeightViewListener {
		public void onEnableChanged(boolean enabled);

		public void onValueChanged(int newValue);
	}

	public NumericWeightView(Composite parent, String title, String tooltip, int defaultWeight, int previousWeightTotal,
			NumericWeightViewListener numericWeightViewListener) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());

		enableButton = createButton(this, SWT.CHECK, title, tooltip);
		layout(enableButton, new FormAttachment(effectiveWeight, 0, SWT.CENTER), new FormAttachment(0, 0));

		weightInput = createSpinner(this, new int[] { defaultWeight, 0, 100, 0, 1, 5 }, true);
		layout(weightInput, new FormAttachment(effectiveWeight, 0, SWT.CENTER), null,
				new FormAttachment(effectiveWeight, -5));

		effectiveWeight = createText(this, SWT.RIGHT,
				String.format("%.2f%%", (double) defaultWeight / (double) (previousWeightTotal + defaultWeight) * 100),
				"", true);
		layout(effectiveWeight, 50, 0, new FormAttachment(0, 0), null, new FormAttachment(100, 0));

		this.listener = numericWeightViewListener;
		cachedValue = defaultWeight;

		enableButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!enableButton.getSelection()) {
					disable();
				} else {
					setWeight(Math.max(1, cachedValue));
				}

				NumericWeightView.this.listener.onEnableChanged(enableButton.getSelection());
			}
		});

		weightInput.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				cachedValue = weightInput.getSelection();
				NumericWeightView.this.listener.onValueChanged(cachedValue);
			}
		});
	}

	public void setSelectable(boolean selectable) {
		enableButton.setEnabled(selectable);

		if (!selectable) {
			disable();
		}
	}

	public void disable() {
		enableButton.setSelection(false);
		weightInput.setSelection(0);
		weightInput.setEnabled(false);
		effectiveWeight.setText("0%");
	}

	public void setWeight(int weight) {
		if (weight == 0) {
			disable();
		} else {
			enableButton.setSelection(true);
			weightInput.setSelection(weight);
			weightInput.setEnabled(true);
			cachedValue = weight;
		}
	}

	public int getWeight() {
		return weightInput.getSelection();
	}

	public void updateWeightTotal(int newTotal) {
		if (getWeight() > 0) {
			effectiveWeight.setText(String.format("%.2f%%", (double) getWeight() / (double) newTotal * 100));
		}
	}
}