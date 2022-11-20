package random.general;

import fedata.gba.GBAFECharacterData;

import java.util.List;

public class GBAStatDAO {
    public int hp;
    public int str;
    public int skl;
    public int spd;
    public int def;
    public int res;
    public int lck;

    public GBAStatDAO(){

    }

    public GBAStatDAO(List<Integer> stats){
        if(stats == null || stats.size() != 7)
            throw new IllegalArgumentException("Wrong number of elements in the given list");

        this.hp = stats.get(0);
        this.str = stats.get(1);
        this.skl = stats.get(2);
        this.spd = stats.get(3);
        this.def = stats.get(4);
        this.res = stats.get(5);
        this.lck = stats.get(6);
    }

    public GBAStatDAO(GBAFECharacterData data){
        this.hp = data.getHPGrowth();
        this.str = data.getSTRGrowth();
        this.skl = data.getSKLGrowth();
        this.spd = data.getSPDGrowth();
        this.def = data.getDEFGrowth();
        this.res = data.getRESGrowth();
        this.lck = data.getLCKGrowth();
    }
}