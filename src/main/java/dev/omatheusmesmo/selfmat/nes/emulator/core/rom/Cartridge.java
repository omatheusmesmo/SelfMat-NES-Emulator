package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

public record Cartridge(
        NESFileHeader NESFileHeader,
        byte[] prgRomData,
        byte[] chrRomData,
        byte[] trainerData
) {
}
