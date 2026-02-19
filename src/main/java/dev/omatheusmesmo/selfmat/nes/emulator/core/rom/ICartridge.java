package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

/**
 * Interface for a NES Cartridge, defining methods for CPU access.
 * This abstraction allows for easier mocking and testing of components
 * that interact with the Cartridge, such as the Bus.
 */
public interface ICartridge {
    NESFileHeader NESFileHeader();
    byte[] prgRomData();
    byte[] chrRomData();
    byte[] trainerData();

    byte cpuRead(int address);
    void cpuWrite(int address, byte data);

    // Methods for the Mapper that RomLoader will load.
    // Although Mapper is abstract, the concrete Cartridge will have an instance.
    Mapper getMapper();
    void loadMapperData(byte[] prgRomData, byte[] chrRomData);
}
