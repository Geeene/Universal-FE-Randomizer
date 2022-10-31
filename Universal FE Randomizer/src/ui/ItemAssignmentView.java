package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import fedata.general.FEBase.GameType;
import ui.model.ItemAssignmentOptions;
import ui.model.ItemAssignmentOptions.ShopAdjustment;
import ui.model.ItemAssignmentOptions.WeaponReplacementPolicy;

public class ItemAssignmentView extends AbstractYuneView {

    private Group container;
    private Button strictWeaponButton;
    private Button rankWeaponButton;
    private Button randomWeaponButton;
    private Button promoWeaponsButton;
    private Button poisonWeaponsButton;

    public ItemAssignmentView(Composite parent, int style, GameType type) {
        super(parent, style);
        setLayout(new FillLayout());

        container = createContainer(this, "Weapon Assignment",
                "Settings for assigning weapons.\nHas no effect if classes are not modified (via class randomization or recruitment randomization)");
        setGroupMargins(container);

        strictWeaponButton = createButton(container, SWT.RADIO, "Strict Matching",
                "Uses the closest analogue to the original weapon as possible.", true, true);
        defaultLayout(strictWeaponButton);

        rankWeaponButton = createButton(container, SWT.RADIO, "Match Rank",
                "Uses any weapon that matches the rank of the original weapon. Adjusts downwards if no exact matches exist.",
                true, false);

        layout(rankWeaponButton, new FormAttachment(strictWeaponButton, 5), new FormAttachment(strictWeaponButton, 0, SWT.LEFT));
        randomWeaponButton = createButton(container, SWT.RADIO, "Random",
                "Uses any weapon that is usable by the character's weapon ranks.", true, false);
        layout(randomWeaponButton, new FormAttachment(rankWeaponButton, 5), new FormAttachment(rankWeaponButton, 0, SWT.LEFT));

        Control lastElement = randomWeaponButton;
        if (type.isGBA() && type != GameType.FE6) {
            String tt = GameType.FE7.equals(type) ? "Allows the assignment of Emblem weapons."
                    : "Allows the assignment of monster slaying weapons.";

            promoWeaponsButton = createButton(container, SWT.CHECK, "Assign Promotional Weapons", tt, true, false);
            layout(promoWeaponsButton, new FormAttachment(lastElement, 10), new FormAttachment(lastElement, 0, SWT.LEFT));
            lastElement = promoWeaponsButton;
        }

        poisonWeaponsButton = createButton(container, SWT.CHECK, "Assign Poison Weapons",
                "Allows the assignment of poison weapons.\nRegardless of this option, enemies may still have them if they had them before.",
                true, false);
        layout(poisonWeaponsButton, new FormAttachment(lastElement, 10),new FormAttachment(lastElement, 0, SWT.LEFT));
    }

    public ItemAssignmentOptions getAssignmentOptions() {
        WeaponReplacementPolicy weaponPolicy;
        if (strictWeaponButton.getSelection()) {
            weaponPolicy = WeaponReplacementPolicy.STRICT;
        } else if (rankWeaponButton.getSelection()) {
            weaponPolicy = WeaponReplacementPolicy.EQUAL_RANK;
        } else if (randomWeaponButton.getSelection()) {
            weaponPolicy = WeaponReplacementPolicy.ANY_USABLE;
        } else {
            assert false : "No Weapon Policy Found.";
            weaponPolicy = WeaponReplacementPolicy.STRICT;
        }

        ShopAdjustment shopPolicy = ShopAdjustment.NO_CHANGE;

        return new ItemAssignmentOptions(weaponPolicy, shopPolicy,
                promoWeaponsButton != null && promoWeaponsButton.getSelection(),
                poisonWeaponsButton.getSelection());
    }

    public void setItemAssignmentOptions(ItemAssignmentOptions options) {
        if (options == null) {
            return;
        }

        strictWeaponButton.setSelection(options.weaponPolicy == WeaponReplacementPolicy.STRICT);
        rankWeaponButton.setSelection(options.weaponPolicy == WeaponReplacementPolicy.EQUAL_RANK);
        randomWeaponButton.setSelection(options.weaponPolicy == WeaponReplacementPolicy.ANY_USABLE);

        if (promoWeaponsButton != null) {
            promoWeaponsButton.setSelection(options.assignPromoWeapons);
        }
        poisonWeaponsButton.setSelection(options.assignPoisonWeapons);
    }
}
