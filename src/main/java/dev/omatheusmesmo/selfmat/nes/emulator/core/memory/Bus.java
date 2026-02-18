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
    private static final int PPU_REGISTERS_MIRROR_MASK = 0x0007; // 0x0007 = 0x0007 (last 3 bits)

    private static final int APU_IO_START_ADDRESS = 0x4000;
    private static final int APU_IO_END_ADDRESS = 0x401F;
    private static final int APU_IO_SIZE = 32;
    private static final int APU_OAM_DMA_ADDRESS = 0x4014;
    private static final int APU_OAM_DMA_SIZE = 256; // OAM DMA transfers 256 bytes from CPU memory to PPU OAM
    private static final int APU_CONTROLLER_1_ADDRESS = 0x4016;
    private static final int APU_CONTROLLER_2_ADDRESS = 0x4017;

    private final byte[] cpuRam = new byte[RAM_SIZE];
    private final byte[] ppuRegisters = new byte[PPU_REGISTERS_SIZE];
    private final byte[] apuRegisters = new byte[APU_IO_SIZE];

    public Bus() {
    }

    /**
     * Reads a byte from the bus at the specified 16-bit address.
     */
    public byte read(int address) {
        address &= ADDRESS_MASK_16BIT; // Ensure 16-bit address

        if (address <= RAM_END_ADDRESS) {
            return cpuRam[address & RAM_MIRROR_MASK];
        } else if (address <= PPU_REGISTERS_END) {
            return ppuRegisters[(address - PPU_REGISTERS_START) & PPU_REGISTERS_MIRROR_MASK];
        } else if (address <= APU_IO_END_ADDRESS) {
            if (address == APU_CONTROLLER_1_ADDRESS){
                return apuRegisters[APU_CONTROLLER_1_ADDRESS - APU_IO_START_ADDRESS];
            } else if (address == APU_CONTROLLER_2_ADDRESS) {
                return apuRegisters[APU_CONTROLLER_2_ADDRESS - APU_IO_START_ADDRESS];
            }else {
                return apuRegisters[address - APU_IO_START_ADDRESS];
            }
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
        } else if (address <= APU_IO_END_ADDRESS) {
            if (address == APU_OAM_DMA_ADDRESS) {
                System.out.println("OAM DMA triggered with data: " + (data & 0xFF));
            }else if (address == APU_CONTROLLER_1_ADDRESS){
                System.out.println("Controller 1 write with data: " + (data & 0xFF));
            } else if (address == APU_CONTROLLER_2_ADDRESS) {
                System.out.println("Controller 2 write with data: " + (data & 0xFF));
            } else {
                apuRegisters[address - APU_IO_START_ADDRESS] = data;
            }
        }

        // Placeholder for other components
    }
}
