package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

public record InstructionMetadata(
        String mnemonic,
        AddressingMode addressingMode,
        int cycles
) {
}
