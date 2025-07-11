package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.factory;

import com.google.auto.service.AutoService;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.MMC1Mapper;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

@AutoService(MapperFactory.class)
public class MMC1Factory implements MapperFactory{

    private static final int SUPPORTED_MAPPER_NUMBER = 1;

    /**
     * Gets the iNES mapper number that this factory supports.
     *
     * @return The supported mapper number.
     */
    @Override
    public int getSupportedMapperNumber() {
        return SUPPORTED_MAPPER_NUMBER;
    }

    /**
     * Creates a new Mapper instance.
     *
     * @param prgRomSizeBytes     Total size of PRG ROM in bytes.
     * @param chrDataSizeBytes    Total size of CHR ROM/RAM in bytes (0 for RAM).
     * @param isVerticalMirroring Initial mirroring type from the header.
     * @return An initialized NRomMapper instance.
     */
    @Override
    public Mapper create(int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        return new MMC1Mapper(getSupportedMapperNumber(), prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
    }
}
