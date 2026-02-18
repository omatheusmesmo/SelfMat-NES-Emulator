package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RomLoader {

    public Cartridge loadRom(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Read header
            List<Byte> header = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                int byteValue = fis.read();
                if (byteValue == -1) {
                    throw new IOException("Unexpected end of file while reading header.");
                }
                header.add((byte) byteValue);
            }

            NESFileHeader NESFileHeader = new NESFileHeader(header);
            if (!NESFileHeader.isValid()) {
                throw new IllegalArgumentException("Invalid NES file header.");
            }

            // Read Trainer (if present)
            byte[] trainerData = null;
            if (NESFileHeader.hasTrainer()) {
                trainerData = new byte[512];
                if (fis.read(trainerData) != 512) {
                    throw new IOException("Unexpected end of file while reading trainer.");
                }
            }

            // Read PRG ROM
            int prgRomSize = NESFileHeader.getPrgRomSize() * 1024;
            byte[] prgRomData = new byte[prgRomSize];
            if (fis.read(prgRomData) != prgRomSize) {
                throw new IOException("Unexpected end of file while reading PRG ROM.");
            }

            // Read CHR ROM
            int chrRomSize = NESFileHeader.getChrRomSize() * 1024;
            byte[] chrRomData = new byte[chrRomSize];
            if (fis.read(chrRomData) != chrRomSize) {
                throw new IOException("Unexpected end of file while reading CHR ROM.");
            }

            Mapper mapper = MapperManager.createMapper(
                    NESFileHeader.getMapperNumber(), prgRomSize, chrRomSize, NESFileHeader.isVerticalMirroring()
            );

            // Return Cartridge
            return new Cartridge(NESFileHeader, prgRomData, chrRomData, trainerData, mapper);
        }
    }
}
