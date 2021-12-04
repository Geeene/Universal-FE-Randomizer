package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import fedata.general.FEBase.GameType;
import ui.model.PromotionOptions;

public class PromotionView extends Composite {
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

		container = new Group(this, SWT.NONE);
		container.setText("Promotions");
		container.setToolTipText("Controls class promotions for all playable characters.");

		FormLayout mainLayout = new FormLayout();
		mainLayout.marginLeft = 5;
		mainLayout.marginRight = 5;
		mainLayout.marginTop = 5;
		mainLayout.marginBottom = 5;
		container.setLayout(mainLayout);

		strictButton = new Button(container, SWT.RADIO);
		strictButton.setText("Default Promotions");
		strictButton.setToolTipText("Sets promotions based on normal class progression.");
		strictButton.setEnabled(true);
		strictButton.setSelection(true);
		strictButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.STRICT, type);
			}
		});

		FormData optionData = new FormData();
		optionData.left = new FormAttachment(0, 0);
		optionData.top = new FormAttachment(0, 0);
		strictButton.setLayoutData(optionData);

		looseButton = new Button(container, SWT.RADIO);
		looseButton.setText("Similar Promotions");
		looseButton.setToolTipText("Sets promotions based on weapon ranks, holy blood, class skills, and base stats.");
		looseButton.setEnabled(true);
		looseButton.setSelection(false);
		looseButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.LOOSE, type);
			}
		});

		optionData = new FormData();
		optionData.left = new FormAttachment(strictButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(strictButton, 15);
		looseButton.setLayoutData(optionData);

		allowMountChangeButton = new Button(container, SWT.CHECK);
		allowMountChangeButton.setText("Allow Mount Change");
		allowMountChangeButton.setToolTipText(
				"Allows mounted units to change between mounts (e.g. flying to horseback, and vice versa).");
		allowMountChangeButton.setEnabled(false);
		allowMountChangeButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(looseButton, 10, SWT.LEFT);
		optionData.top = new FormAttachment(looseButton, 5);
		allowMountChangeButton.setLayoutData(optionData);
		Button lastButton = allowMountChangeButton;

		if (GameType.FE4.equals(type)) {
			allowEnemyClassButton = new Button(container, SWT.CHECK);
			allowEnemyClassButton.setText("Allow Enemy-only Promotions");
			allowEnemyClassButton
					.setToolTipText("Allows units to promote into enemy-only classes like Baron, Queen, and Emperor.");
			allowEnemyClassButton.setEnabled(false);
			allowEnemyClassButton.setSelection(false);

			optionData = new FormData();
			optionData.left = new FormAttachment(allowMountChangeButton, 0, SWT.LEFT);
			optionData.top = new FormAttachment(allowMountChangeButton, 5);
			allowEnemyClassButton.setLayoutData(optionData);
			lastButton = allowEnemyClassButton;
		}

		randomButton = new Button(container, SWT.RADIO);
		randomButton.setText("Random Promotions");
		randomButton.setToolTipText("Sets promotions enitrely randomly.");
		randomButton.setEnabled(true);
		randomButton.setSelection(false);
		randomButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setMode(PromotionOptions.Mode.RANDOM, type);
			}
		});

		optionData = new FormData();
		optionData.left = new FormAttachment(looseButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(lastButton, 15);
		randomButton.setLayoutData(optionData);

		commonWeaponButton = new Button(container, SWT.CHECK);
		commonWeaponButton.setText("Requires Common Weapon");
		commonWeaponButton
				.setToolTipText("Requires the promoted class to share at least one weapon type with its predecessor.");
		commonWeaponButton.setEnabled(false);
		commonWeaponButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(randomButton, 10, SWT.LEFT);
		optionData.top = new FormAttachment(randomButton, 5);
		commonWeaponButton.setLayoutData(optionData);

		keepDamageTypeButton = new Button(container, SWT.CHECK);
		keepDamageTypeButton.setText("Keep Same Damage Type");
		keepDamageTypeButton.setToolTipText(
				"Magical classes will promote into random magical classes, while physical classes will stay in physical classes.");
		keepDamageTypeButton.setEnabled(false);
		keepDamageTypeButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(randomButton, 10, SWT.LEFT);
		optionData.top = new FormAttachment(commonWeaponButton, 5);
		keepDamageTypeButton.setLayoutData(optionData);

		if (type.equals(GameType.FE8)) {
			allowMonsterClassButton = new Button(container, SWT.CHECK);
			allowMonsterClassButton.setText("Allow Monster Promotions");
			allowMonsterClassButton.setToolTipText(
					"Allows units to promote into Monster classes like Elder Bael, Wight, and Arch Mogall.");
			allowMonsterClassButton.setEnabled(false);
			allowMonsterClassButton.setSelection(false);

			optionData = new FormData();
			optionData.left = new FormAttachment(randomButton, 0, SWT.LEFT);
			optionData.top = new FormAttachment(keepDamageTypeButton, 5);
			allowMonsterClassButton.setLayoutData(optionData);
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
