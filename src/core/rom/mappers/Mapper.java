package core.rom.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Mapper {

    public int mapperNumber;
    public int prgRomSize;
    public int prgRomBankSize;
    public int prgRamSize;
    public int chrRamSize;
    public int chrRomSize;
    public boolean isVerticalMirroring;
    public Map<Integer, Integer> banks = new HashMap<>();

   public Mapper(){
       this.banks.put(0x8000, 0xBFFF); // first prg rom
       this.banks.put(0XC000, 0xFFFF);// second prg rom
       this.banks.put(0x6000, 0x7FFF); // prg ram
   }

}
