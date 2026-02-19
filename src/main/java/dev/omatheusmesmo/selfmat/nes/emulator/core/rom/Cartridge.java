package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

/**
 * Represents a NES game cartridge, holding its ROM data and the Mapper logic.
 * This record acts as a facade for the Mapper, delegating CPU read/write operations.
 * Implements ICartridge interface for abstraction.
 */
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
