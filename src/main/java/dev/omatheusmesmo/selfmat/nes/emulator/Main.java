package dev.omatheusmesmo.selfmat.nes.emulator;

import dev.omatheusmesmo.selfmat.nes.emulator.core.memory.Bus;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.RomLoader;

public class Main {
    public static void main(String[] args) {
        try {
            String filePath = "/home/omatheusmesmo/Downloads/Super Mario Bros. (World).nes";

            // Carrega a ROM
            RomLoader loader = new RomLoader();
            ICartridge cart = loader.loadRom(filePath);

            // Cria o barramento e conecta o cartucho
            Bus bus = new Bus(cart);

            // Testa algumas leituras básicas via Bus
            System.out.println("\nTesting Bus reads (via Cartridge delegation):");
            // Estes testes aqui simulam a CPU lendo diretamente do Bus
            // e o Bus delegando para o Mapper (via Cartridge)
            System.out.printf("PRG ROM read at $8000: 0x%02X\n", bus.read(0x8000) & 0xFF);
            System.out.printf("PRG ROM read at $FFFF: 0x%02X\n", bus.read(0xFFFF) & 0xFF);
            // Para CHR ROM, teríamos que ler via PPU Bus ou ter um método específico no Cartridge para isso.
            // Por enquanto, apenas para demonstração do Bus.

        } catch (Exception e) {
            System.err.println("Error loading ROM: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
