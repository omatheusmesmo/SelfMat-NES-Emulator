package dev.omatheusmesmo.selfmat.nes.emulator.core.memory;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;

/**
 * The Bus connects all components of the NES (CPU, RAM, PPU, APU, Cartridge).
 * It handles the memory mapping logic.
 */
public class Bus {

    // --- Constants for Memory Mapping ---
    private static final int RAM_START_ADDRESS = 0x0000;
    private static final int RAM_END_ADDRESS = 0x1FFF; // 2KB RAM mirrored 4 times
    private static final int RAM_SIZE = 2048; // 2KB
    private static final int RAM_MIRROR_MASK = 0x07FF; // Mask to extract lowest 11 bits, mapping mirrored addresses to 2KB RAM
    private static final int ADDRESS_MASK_16BIT = 0xFFFF;
    private static final int ADDRESS_HIGH_BITS_MASK = 0xF000; // Mask to get the highest 4 bits (for switch cases)
    private static final int BLOCK_4KB_SIZE = 0x1000; // 4KB block size for switch cases

    private static final int PPU_REGISTERS_START = 0x2000;
    private static final int PPU_REGISTERS_END = 0x3FFF; // 8 PPU registers mirrored every 8 bytes
    private static final int PPU_REGISTERS_SIZE = 8;
    private static final int PPU_REGISTERS_MIRROR_MASK = 0x0007; // Mask to extract lowest 3 bits, mirroring 8 PPU registers across 0x2000-0x3FFF range

    private static final int APU_IO_START_ADDRESS = 0x4000;
    private static final int APU_IO_END_ADDRESS = 0x401F;
    private static final int APU_IO_SIZE = 32;
    private static final int APU_OAM_DMA_ADDRESS = 0x4014;
    private static final int APU_CONTROLLER_1_ADDRESS = 0x4016;
    private static final int APU_CONTROLLER_2_ADDRESS = 0x4017;

    private static final int CARTRIDGE_END_ADDRESS = 0xFFFF; // End of CPU address space (0xFFFF)

    private final byte[] cpuRam = new byte[RAM_SIZE];
    private final byte[] ppuRegisters = new byte[PPU_REGISTERS_SIZE];
    private final byte[] apuRegisters = new byte[APU_IO_SIZE];
    private final ICartridge cartridge; // Now uses the interface

    public Bus(ICartridge cartridge) {
        this.cartridge = java.util.Objects.requireNonNull(cartridge, "Cartridge cannot be null");
    }

    /**
     * Reads a byte from the bus at the specified 16-bit address.
     */
    public byte read(int address) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        // Use a switch on the high bits of the address for block routing
        // This is a common way to simplify memory mapping logic.
        // The cases are based on 4KB blocks (0x1000) for finer grain.
        switch (address & ADDRESS_HIGH_BITS_MASK) { // Check the highest 4 bits (e.g., 0x0000, 0x1000, 0x2000, etc.)
            case RAM_START_ADDRESS: // 0x0000 - 0x0FFF: CPU RAM and its mirror
            case RAM_START_ADDRESS + BLOCK_4KB_SIZE: // 0x1000 - 0x1FFF: CPU RAM mirror
                return cpuRam[address & RAM_MIRROR_MASK];
            case PPU_REGISTERS_START: // 0x2000 - 0x2FFF: PPU registers and its mirror
            case PPU_REGISTERS_START + BLOCK_4KB_SIZE: // 0x3000 - 0x3FFF: PPU mirror
                // Need to normalize to 0x2000-0x2007 for PPU logic, then apply mirror mask
                int ppuReadIndex = (address - PPU_REGISTERS_START) & PPU_REGISTERS_MIRROR_MASK;
                return ppuRegisters[ppuReadIndex];
            case APU_IO_START_ADDRESS: // 0x4000 - 0x4FFF: APU/IO and Cartridge
                // Now, check specific sub-ranges within this 4K block
                if (address <= APU_IO_END_ADDRESS) {
                    // APU/IO Registers (0x4000 - 0x401F)
                    if (address == APU_CONTROLLER_1_ADDRESS) {
                        return apuRegisters[APU_CONTROLLER_1_ADDRESS - APU_IO_START_ADDRESS]; // Stub value
                    } else if (address == APU_CONTROLLER_2_ADDRESS) {
                        return apuRegisters[APU_CONTROLLER_2_ADDRESS - APU_IO_START_ADDRESS]; // Stub value
                    } else {
                        return apuRegisters[address - APU_IO_START_ADDRESS];
                    }
                }
                // Addresses 0x4020-0x4FFF fall through to cartridge
            default: // Catches 0x4020 - 0xFFFF (mostly Cartridge space)
                // Delegate to cartridge for read
                return cartridge.cpuRead(address);
        }
    }

    /**
     * Writes a byte to the bus at the specified 16-bit address.
     */
    public void write(int address, byte data) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        switch (address & ADDRESS_HIGH_BITS_MASK) { // Check the highest 4 bits
            case RAM_START_ADDRESS: // 0x0000 - 0x0FFF: CPU RAM and its mirror
            case RAM_START_ADDRESS + BLOCK_4KB_SIZE: // 0x1000 - 0x1FFF: CPU RAM mirror
                cpuRam[address & RAM_MIRROR_MASK] = data;
                break;
            case PPU_REGISTERS_START: // 0x2000 - 0x2FFF: PPU registers and its mirror
            case PPU_REGISTERS_START + BLOCK_4KB_SIZE: // 0x3000 - 0x3FFF: PPU mirror
                int ppuWriteIndex = (address - PPU_REGISTERS_START) & PPU_REGISTERS_MIRROR_MASK;
                ppuRegisters[ppuWriteIndex] = data;
                break;
            case APU_IO_START_ADDRESS: // 0x4000 - 0x4FFF: APU/IO and Cartridge
                if (address <= APU_IO_END_ADDRESS) {
                    // APU/IO Registers (0x4000 - 0x401F)
                    if (address == APU_OAM_DMA_ADDRESS) {
                        System.out.println("OAM DMA triggered with data: " + (data & 0xFF));
                    } else if (address == APU_CONTROLLER_1_ADDRESS) {
                        System.out.println("Controller 1 write with data: " + (data & 0xFF));
                    } else if (address == APU_CONTROLLER_2_ADDRESS) {
                        System.out.println("Controller 2 write with data: " + (data & 0xFF));
                    } else {
                        apuRegisters[address - APU_IO_START_ADDRESS] = data;
                    }
                } else {
                    // Fallthrough to Cartridge if not APU/IO (within 0x4000 block)
                    cartridge.cpuWrite(address, data);
                }
                break;
            default: // Catches 0x5000 - 0xFFFF (mostly Cartridge space)
                // Delegate to cartridge for write
                cartridge.cpuWrite(address, data);
                break;
        }
    }
}