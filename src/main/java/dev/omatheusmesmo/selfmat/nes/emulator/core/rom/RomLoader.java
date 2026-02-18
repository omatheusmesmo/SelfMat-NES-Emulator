package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RomLoader is responsible for loading a NES ROM file (.nes) from the filesystem,
 * parsing its header, extracting PRG and CHR ROM data, and creating a Cartridge object.
 */
public class RomLoader {

    /**
     * Loads a NES ROM from the specified file path.
     *
     * @param filePath The path to the .nes file.
     * @return A fully loaded ICartridge object.
     * @throws IOException If there's an error reading the file.
     * @throws IllegalArgumentException If the NES file header is invalid or data is incomplete.
     */
    public ICartridge loadRom(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Read header (first 16 bytes)
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

            // Read Trainer (if present, 512 bytes)
            byte[] trainerData = null;
            if (NESFileHeader.hasTrainer()) {
                trainerData = new byte[512];
                if (fis.read(trainerData) != 512) {
                    throw new IOException("Unexpected end of file while reading trainer.");
                }
            }

            // Read PRG ROM (Program ROM)
            int prgRomSize = NESFileHeader.getPrgRomSize() * 1024; // Size in bytes
            byte[] prgRomData = new byte[prgRomSize];
            if (fis.read(prgRomData) != prgRomSize) {
                throw new IOException("Unexpected end of file while reading PRG ROM.");
            }

            // Read CHR ROM (Character ROM or Pattern Tables)
            int chrRomSize = NESFileHeader.getChrRomSize() * 1024; // Size in bytes
            byte[] chrRomData = new byte[chrRomSize];
            if (fis.read(chrRomData) != chrRomSize) {
                throw new IOException("Unexpected end of file while reading CHR ROM.");
            }

            // Create the appropriate Mapper instance
            Mapper mapper = MapperManager.createMapper(
                    NESFileHeader.getMapperNumber(), prgRomSize, chrRomSize, NESFileHeader.isVerticalMirroring()
            );

            // Load ROM data into the mapper
            mapper.loadRomData(prgRomData, chrRomData);

            // Return the fully constructed Cartridge
            return new Cartridge(NESFileHeader, prgRomData, chrRomData, trainerData, mapper);
        }
    }
}