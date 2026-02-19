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

    /**
     * Deprecated: Mapper ROM data is already loaded during cartridge creation.
     * <p>
     * This method is intentionally a no-op to avoid reloading the same data
     * into the mapper, which is handled by the ROM loader before constructing
     * the {@link Cartridge}.
     */
    @Deprecated
    @Override
    public void loadMapperData(byte[] prgRomData, byte[] chrRomData) {
        // no-op: mapper ROM data is initialized by the ROM loader
    }
}
