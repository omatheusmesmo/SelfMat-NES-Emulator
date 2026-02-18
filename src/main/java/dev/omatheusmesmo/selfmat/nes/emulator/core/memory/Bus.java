package dev.omatheusmesmo.selfmat.nes.emulator.core.memory;

/**
 * The Bus connects all components of the NES (CPU, RAM, PPU, APU, Cartridge).
 * It handles the memory mapping logic.
 */
public class Bus {

    // --- Constants for Memory Mapping ---
    private static final int RAM_START_ADDRESS = 0x0000;
    private static final int RAM_END_ADDRESS = 0x1FFF; // 2KB RAM mirrored 4 times
    private static final int RAM_SIZE = 2048; // 2KB
    private static final int RAM_MIRROR_MASK = 0x07FF; // 0x1FFF & 0x07FF = 0x07FF (last 11 bits)
    private static final int ADDRESS_MASK_16BIT = 0xFFFF;

    private static final int PPU_REGISTERS_START = 0x2000;
    private static final int PPU_REGISTERS_END = 0x3FFF; // 8 PPU registers mirrored every 8 bytes
    private static final int PPU_REGISTERS_SIZE = 8;
    private static final int PPU_REGISTERS_MIRROR_MASK = 0x0007; // 0x3FFF & 0x0007 = 0x0007 (last 3 bits)

    // For now, a simple 64KB RAM array to represent the full address space
    // In the future, this will dispatch calls to RAM, PPU, Mappers, etc.
    private final byte[] cpuRam = new byte[RAM_SIZE]; // 2KB Internal RAM

    private final byte[] ppuRegisters = new byte[PPU_REGISTERS_SIZE];

    public Bus() {
    }

    /**
     * Reads a byte from the bus at the specified 16-bit address.
     */
    public byte read(int address) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        // 0x0000 - 0x1FFF: 2KB Internal RAM (mirrored 4 times)
        if (address <= RAM_END_ADDRESS) {
            return cpuRam[address & RAM_MIRROR_MASK];
        } else if (address <= PPU_REGISTERS_END) {
            return ppuRegisters[(address - PPU_REGISTERS_START) & PPU_REGISTERS_MIRROR_MASK];
        }

        // Placeholder for other components (PPU, APU, Mappers)
        return 0;
    }

    /**
     * Writes a byte to the bus at the specified 16-bit address.
     */
    public void write(int address, byte data) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        // 0x0000 - 0x1FFF: 2KB Internal RAM (mirrored 4 times)
        if (address <= RAM_END_ADDRESS) {
            cpuRam[address & RAM_MIRROR_MASK] = data;
        }else if (address <= PPU_REGISTERS_END) {
            int ppuRegisterIndex = (address - PPU_REGISTERS_START) & PPU_REGISTERS_MIRROR_MASK; // Mirror every 8 bytes
            ppuRegisters[ppuRegisterIndex] = data;
        }

        // Placeholder for other components
    }
}
