package random.general;

import fedata.gba.GBAFECharacterData;

import java.util.Arrays;
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

    public GBAStatDAO(int... stats){
        if(stats == null || stats.length != 7)
            throw new IllegalArgumentException("Wrong number of elements in the given list");

        this.hp = stats[0];
        this.str = stats[1];
        this.skl = stats[2];
        this.spd = stats[3];
        this.def = stats[4];
        this.res = stats[5];
        this.lck = stats[6];
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
    
    public List<Integer> getAll(){
    	return Arrays.asList(hp, str, skl, spd, def, res, lck);
    }
    
}