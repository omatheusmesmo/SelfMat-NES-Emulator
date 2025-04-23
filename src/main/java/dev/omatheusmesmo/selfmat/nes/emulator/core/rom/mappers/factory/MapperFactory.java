package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.factory;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

/**
 * Interface for factories that create specific Mapper instances.
 * Implementations should be discoverable via ServiceLoader.
 */
public interface MapperFactory {

    /**
     * Gets the iNES mapper number that this factory supports.
     * @return The supported mapper number.
     */
    int getSupportedMapperNumber();

    /**
     * Creates a new Mapper instance.
     *
     * @param prgRomSizeBytes     Total size of PRG ROM in bytes.
     * @param chrDataSizeBytes    Total size of CHR ROM/RAM in bytes (0 for RAM).
     * @param isVerticalMirroring Initial mirroring type from the header.
     * @return An initialized Mapper instance.
     */
    Mapper create(int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring);
}
    