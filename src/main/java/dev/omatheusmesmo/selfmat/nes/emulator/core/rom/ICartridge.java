package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

/**
 * Interface for a NES Cartridge, defining methods for CPU access.
 * This abstraction allows for easier mocking and testing of components
 * that interact with the Cartridge, such as the Bus.
 */
public interface ICartridge {
    byte cpuRead(int address);
    void cpuWrite(int address, byte data);
    
    // Potentially add other methods like ppuRead, ppuWrite if needed later
    // NESFileHeader NESFileHeader();
    // byte[] prgRomData();
    // byte[] chrRomData();
    // byte[] trainerData();

    // Métodos para o Mapper que a RomLoader irá carregar.
    // Embora Mapper seja abstrato, o Cartridge concreto terá uma instância.
    Mapper getMapper();
    void loadMapperData(byte[] prgRomData, byte[] chrRomData);
}
