package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import fedata.general.FEBase.GameType;
import ui.model.PromotionOptions;

public class PromotionView extends AbstractYuneView {
	private Group container;

	private Button strictButton;

	private Button looseButton;
	private Button allowMountChangeButton;
	private Button allowEnemyClassButton;
	private Button allowMonsterClassButton;

	private Button randomButton;
	private Button commonWeaponButton;
	private Button keepDamageTypeButton;

	private PromotionOptions.Mode currentMode;

	public PromotionView(Composite parent, int style, GameType type) {
		super(parent, style);

		setLayout(new FillLayout());

		container = createContainer(this, "Promotions", "Controls class promotions for all playable characters.");
		setGroupMargins(container);

		strictButton = createButton(container, SWT.RADIO, "Default Promotions",
				"Sets promotions based on normal class progression.", true, true);
		defaultLayout(strictButton);
		strictButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.STRICT, type);
			}
		});

		looseButton = new Button(container, SWT.RADIO);
		looseButton.setText("Similar Promotions");
		if (GameType.FE4.equals(type)) {
			looseButton
					.setToolTipText("Sets promotions based on weapon ranks, holy blood, class skills, and base stats.");
		} else {
			looseButton.setToolTipText(
					"Sets promotions primarily based on weapon ranks. \nnot recommended, heavily limited options especially for FE6.");
		}
		looseButton.setEnabled(true);
		looseButton.setSelection(false);
		looseButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.LOOSE, type);
			}
		});
		layout(looseButton, new FormAttachment(strictButton, 15), new FormAttachment(strictButton, 0, SWT.LEFT));
		allowMountChangeButton = createButton(container, SWT.CHECK, "Allow Mount Change",
				"Allows mounted units to change between mounts (e.g. flying to horseback, and vice versa).");
		layout(allowMountChangeButton, new FormAttachment(looseButton, 5),
				new FormAttachment(looseButton, 10, SWT.LEFT));
		Button lastButton = allowMountChangeButton;

		if (GameType.FE4.equals(type)) {
			allowEnemyClassButton = createButton(container, SWT.CHECK, "Allow Enemy-only Promotions",
					"Allows units to promote into enemy-only classes like Baron, Queen, and Emperor.");
			allowEnemyClassButton.setSelection(false);
			layout(allowEnemyClassButton, new FormAttachment(allowMountChangeButton, 5),
					new FormAttachment(allowMountChangeButton, 0, SWT.LEFT));
			lastButton = allowEnemyClassButton;
		}

		randomButton = createButton(container, SWT.RADIO, "Random Promotions", "Sets promotions enitrely randomly.",
				true, false);
		layout(randomButton, new FormAttachment(lastButton, 15), new FormAttachment(looseButton, 0, SWT.LEFT));
		randomButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.RANDOM, type);
			}
		});
		commonWeaponButton = createButton(container, SWT.CHECK, "Requires Common Weapon",
				"Requires the promoted class to share at least one weapon type with its predecessor.");
		layout(commonWeaponButton, new FormAttachment(randomButton, 5), new FormAttachment(randomButton, 10, SWT.LEFT));
		if (type.isGBA()) {
			keepDamageTypeButton = createButton(container, SWT.CHECK, "Keep Same Damage Type",
					"Magical classes will promote into random magical classes, while physical classes will stay in physical classes.");
			layout(keepDamageTypeButton, new FormAttachment(commonWeaponButton, 5),
					new FormAttachment(randomButton, 10, SWT.LEFT));
		}

		if (type.equals(GameType.FE8)) {
			allowMonsterClassButton = createButton(container, SWT.CHECK, "Allow Monster Promotions",
					"Allows units to promote into Monster classes like Elder Bael, Wight, and Arch Mogall.");
			layout(allowMonsterClassButton, new FormAttachment(keepDamageTypeButton, 5),
					new FormAttachment(randomButton, 0, SWT.LEFT));
		}
	}

	private void setMode(PromotionOptions.Mode mode, GameType type) {
		currentMode = mode;

		allowMountChangeButton.setEnabled(currentMode == PromotionOptions.Mode.LOOSE);
		if (GameType.FE4.equals(type)) {
			allowEnemyClassButton.setEnabled(currentMode == PromotionOptions.Mode.LOOSE);
		}
		if (type.isGBA()) {
			keepDamageTypeButton.setEnabled(currentMode == PromotionOptions.Mode.RANDOM);
			if (GameType.FE8.equals(type)) {
				allowMonsterClassButton.setEnabled(true);
			}
		}
		commonWeaponButton.setEnabled(currentMode == PromotionOptions.Mode.RANDOM);
	}

	public PromotionOptions getPromotionOptions() {
		return new PromotionOptions(currentMode, allowMountChangeButton.getSelection(),
				allowEnemyClassButton == null ? null : allowEnemyClassButton.getSelection(),
				commonWeaponButton.getSelection(),
				allowMonsterClassButton == null ? null : allowMonsterClassButton.getSelection(),
				keepDamageTypeButton == null ? null : keepDamageTypeButton.getSelection());
	}

	public void setPromotionOptions(PromotionOptions options, GameType type) {
		if (options == null) {
			// Shouldn't happen.
		} else {
			currentMode = options.promotionMode;

			if (currentMode == null) {
				currentMode = PromotionOptions.Mode.STRICT;
			}

			strictButton.setSelection(currentMode == PromotionOptions.Mode.STRICT);
			looseButton.setSelection(currentMode == PromotionOptions.Mode.LOOSE);
			allowMountChangeButton.setEnabled(currentMode == PromotionOptions.Mode.LOOSE);
			allowMountChangeButton.setSelection(options.allowMountChanges);
			if (type.equals(GameType.FE4)) {
				allowEnemyClassButton.setEnabled(currentMode == PromotionOptions.Mode.LOOSE);
				allowEnemyClassButton.setSelection(options.allowEnemyOnlyPromotedClasses);
			}

			if (type.isGBA()) {
				if (type.equals(GameType.FE8)) {
					allowMonsterClassButton.setEnabled(true);
					allowMonsterClassButton.setSelection(options.allowMonsterClasses);
				}
				keepDamageTypeButton.setEnabled(currentMode == PromotionOptions.Mode.RANDOM);
				keepDamageTypeButton.setSelection(options.keepSameDamageType);
			}

			randomButton.setSelection(currentMode == PromotionOptions.Mode.RANDOM);
			commonWeaponButton.setEnabled(currentMode == PromotionOptions.Mode.RANDOM);
			commonWeaponButton.setSelection(options.requireCommonWeapon);
		}
	}
}
