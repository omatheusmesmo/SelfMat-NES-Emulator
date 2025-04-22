package main.java.dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers;

import java.util.HashMap;
import java.util.Map;

public abstract class Mapper {

    public int mapperNumber;
    public int prgRomSize;
    public int prgRomBankSize;
    public int prgRamSize;
    public int chrRamSize;
    public int chrRomSize;
    public boolean isVerticalMirroring;

   public Mapper(
           int mapperNumber, int prgRomSize,
           int prgRomBankSize, int prgRamSize,
           int chrRamSize, int chrRomSize,
           boolean isVerticalMirroring
   ){
       this.mapperNumber = mapperNumber;
       this.prgRomSize = prgRomSize;
       this.prgRomBankSize = prgRomBankSize;
       this.prgRamSize = prgRamSize;
       this.chrRamSize = chrRamSize;
       this.chrRomSize = chrRomSize;
       this.isVerticalMirroring = isVerticalMirroring;
   }

   public abstract int mapCpuReadAddress(int address);

   public abstract byte cpuRead(int address);

   public abstract void cpuWrite(int address, byte data);

    public abstract byte ppuRead(int address);

    public abstract void ppuWrite(int address, byte data);

}
