package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

public enum AddressingMode {
    IMMEDIATE, ZERO_PAGE, ZERO_PAGE_X,
    ZERO_PAGE_Y, ABSOLUTE, ABSOLUTE_X,
    ABSOLUTE_Y, INDIRECT, INDEXED_INDIRECT,
    INDIRECT_INDEXED, ACCUMULATOR, IMPLIED,
    RELATIVE, UNKNOWN
}
