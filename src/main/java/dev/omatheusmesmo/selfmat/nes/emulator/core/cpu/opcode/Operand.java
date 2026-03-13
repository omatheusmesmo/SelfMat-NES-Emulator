package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

/**
 * Represents an operand for a 6502 instruction.
 * Contains the resolved effective address and whether a page boundary was crossed
 * during address resolution.
 */
public record Operand(int address, boolean pageCrossed) {
}
