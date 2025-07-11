package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.factory;

import com.google.auto.service.AutoService;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.MMC3Mapper;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

@AutoService(MapperFactory.class)
public class MMC3Factory implements MapperFactory {

    private static final int SUPPORTED_MAPPER_NUMBER = 4;

    @Override
    public int getSupportedMapperNumber() {
        return SUPPORTED_MAPPER_NUMBER;
    }

    @Override
    public Mapper create(int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        return new MMC3Mapper(getSupportedMapperNumber(), prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
    }
}