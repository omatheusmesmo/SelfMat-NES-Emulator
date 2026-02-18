package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

public record Cartridge(
        NESFileHeader NESFileHeader,
        byte[] prgRomData,
        byte[] chrRomData,
        byte[] trainerData,
        Mapper mapper
) {

    public byte cpuRead(int address){
        return mapper.cpuRead(address);
    }

    public void cpuWrite(int address, byte cpu){
        mapper.cpuWrite(address, cpu);
    }
}
