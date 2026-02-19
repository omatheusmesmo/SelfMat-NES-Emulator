package dev.omatheusmesmo.selfmat.nes.emulator;

import dev.omatheusmesmo.selfmat.nes.emulator.core.memory.Bus;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.RomLoader;

public class Main {
    public static void main(String[] args) {
        try {
            String filePath = "/home/omatheusmesmo/Downloads/Super Mario Bros. (World).nes";

            // Load the ROM
            RomLoader loader = new RomLoader();
            ICartridge cart = loader.loadRom(filePath);

            // Display header information
            System.out.println("ROM Header Info:");
            System.out.println("PRG ROM Size: " + cart.NESFileHeader().getPrgRomSize() + "KB");
            System.out.println("CHR ROM Size: " + cart.NESFileHeader().getChrRomSize() + "KB");
            System.out.println("Mapper Number: " + cart.NESFileHeader().getMapperNumber());
            System.out.println("Mirroring: " + (cart.NESFileHeader().isVerticalMirroring() ? "Vertical" : "Horizontal"));
            System.out.println("Has Battery: " + cart.NESFileHeader().usesBattery());
            System.out.println("Has Trainer: " + cart.NESFileHeader().hasTrainer());

            // Create the bus and connect the cartridge
            Bus bus = new Bus(cart);

            // Test some basic reads via Bus
            System.out.println("\nTesting Bus reads (via Cartridge delegation):");
            // These tests simulate the CPU reading directly from the Bus
            // and the Bus delegating to the Mapper (via Cartridge)
            System.out.printf("PRG ROM read at $8000: 0x%02X\n", bus.read(0x8000) & 0xFF);
            System.out.printf("PRG ROM read at $FFFF: 0x%02X\n", bus.read(0xFFFF) & 0xFF);
            // For CHR ROM, we would need to read via PPU Bus or have a specific method in Cartridge for that.
            // For now, just for Bus demonstration.

        } catch (Exception e) {
            System.err.println("Error loading ROM: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
