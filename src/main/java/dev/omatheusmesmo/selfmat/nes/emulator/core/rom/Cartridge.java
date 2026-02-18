package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

public record Cartridge(
        NESFileHeader NESFileHeader,
        byte[] prgRomData,
        byte[] chrRomData,
        byte[] trainerData,
        Mapper mapper
) implements ICartridge {

    @Override
    public byte cpuRead(int address){
        return mapper.cpuRead(address);
    }

    @Override
    public void cpuWrite(int address, byte data){
        mapper.cpuWrite(address, data);
    }

    @Override
    public Mapper getMapper() {
        return mapper;
    }

    @Override
    public void loadMapperData(byte[] prgRomData, byte[] chrRomData) {
        mapper.loadRomData(prgRomData, chrRomData);
    }
}
