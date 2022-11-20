package random.general;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GBAWeaponRankDAO {
    public int sword;
    public int lance;
    public int axe;
    public int bow;
    public int anima;
    public int dark;
    public int light;
    public int staff;

    public GBAWeaponRankDAO(List<Integer> ranks){
        if(ranks == null || ranks.size() != 8)
            throw new IllegalArgumentException("Wrong number of elements in the given list");

        this.sword = ranks.get(0);
        this.lance = ranks.get(1);
        this.axe = ranks.get(2);
        this.bow = ranks.get(3);
        this.anima = ranks.get(4);
        this.dark = ranks.get(5);
        this.light = ranks.get(6);
        this.staff = ranks.get(7);
    }

    public List<Integer> getAll(){
        return Arrays.asList(sword, lance, axe, bow, anima, dark, light, staff);
    }
    public List<Integer> getAllSorted(){
        List<Integer> ranks = getAll();
        Collections.sort(ranks);
        return ranks;
    }

    public int countUsableTypes() {
        return (int) getAll().stream().filter(wr -> wr > 0).count();
    }
}