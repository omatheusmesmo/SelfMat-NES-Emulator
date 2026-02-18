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

    // For now, a simple 64KB RAM array to represent the full address space
    // In the future, this will dispatch calls to RAM, PPU, Mappers, etc.
    private final byte[] cpuRam = new byte[RAM_SIZE]; // 2KB Internal RAM

    public Bus() {
    }

    /**
     * Reads a byte from the bus at the specified 16-bit address.
     */
    public byte read(int address) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        // 0x0000 - 0x1FFF: 2KB Internal RAM (mirrored 4 times)
        if (address >= RAM_START_ADDRESS && address <= RAM_END_ADDRESS) {
            return cpuRam[address & RAM_MIRROR_MASK];
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
        if (address >= RAM_START_ADDRESS && address <= RAM_END_ADDRESS) {
            cpuRam[address & RAM_MIRROR_MASK] = data;
        }

        // Placeholder for other components
    }
}
