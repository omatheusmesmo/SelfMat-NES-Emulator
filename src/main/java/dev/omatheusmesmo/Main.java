package dev.omatheusmesmo;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.Cartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.RomLoader;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.MapperManager;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;

public class Main {
    public static void main(String[] args) {
        try {
            String filePath = "/home/omatheusmesmo/Downloads/Super Mario Bros. (World).nes";

            // Carrega a ROM
            RomLoader loader = new RomLoader();
            Cartridge cart = loader.loadRom(filePath);

            // Exibe informações do header
            System.out.println("ROM Header Info:");
            System.out.println("PRG ROM Size: " + cart.NESFileHeader().getPrgRomSize() + "KB");
            System.out.println("CHR ROM Size: " + cart.NESFileHeader().getChrRomSize() + "KB");
            System.out.println("Mapper Number: " + cart.NESFileHeader().getMapperNumber());
            System.out.println("Mirroring: " + (cart.NESFileHeader().isVerticalMirroring() ? "Vertical" : "Horizontal"));
            System.out.println("Has Battery: " + cart.NESFileHeader().usesBattery());
            System.out.println("Has Trainer: " + cart.NESFileHeader().hasTrainer());

            // Cria o mapper apropriado
            Mapper mapper = MapperManager.createMapper(
                    cart.NESFileHeader().getMapperNumber(),
                    cart.prgRomData().length,
                    cart.chrRomData().length,
                    cart.NESFileHeader().isVerticalMirroring()
            );

            // Carrega os dados no mapper
            mapper.loadRomData(cart.prgRomData(), cart.chrRomData());

            // Testa algumas leituras básicas
            System.out.println("\nTesting mapper reads:");
            System.out.printf("PRG ROM read at $8000: 0x%02X\n", mapper.cpuRead(0x8000) & 0xFF);
            System.out.printf("PRG ROM read at $FFFF: 0x%02X\n", mapper.cpuRead(0xFFFF) & 0xFF);
            System.out.printf("CHR ROM read at $0000: 0x%02X\n", mapper.ppuRead(0x0000) & 0xFF);
            System.out.printf("CHR ROM read at $1FFF: 0x%02X\n", mapper.ppuRead(0x1FFF) & 0xFF);

        } catch (Exception e) {
            System.err.println("Error loading ROM: " + e.getMessage());
            e.printStackTrace();
        }
    }
}