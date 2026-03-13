package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

import static dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.AddressingMode.*;

public final class Opcodes {

    private static final int BYTE_MASK = 0xFF;
    private final static InstructionMetadata[] OPCODES = new InstructionMetadata[256];

    private Opcodes() {
        // Utility class
    }

    public static InstructionMetadata getInstructionMetadata(int opcode) {
        return OPCODES[opcode & BYTE_MASK];
    }

    static {
        // ADC
        OPCODES[0x69] = new InstructionMetadata("ADC", IMMEDIATE, 2);
        OPCODES[0x65] = new InstructionMetadata("ADC", ZERO_PAGE, 3);
        OPCODES[0x75] = new InstructionMetadata("ADC", ZERO_PAGE_X, 4);
        OPCODES[0x6D] = new InstructionMetadata("ADC", ABSOLUTE, 4);
        OPCODES[0x7D] = new InstructionMetadata("ADC", ABSOLUTE_X, 4);
        OPCODES[0x79] = new InstructionMetadata("ADC", ABSOLUTE_Y, 4);
        OPCODES[0x61] = new InstructionMetadata("ADC", INDEXED_INDIRECT, 6);
        OPCODES[0x71] = new InstructionMetadata("ADC", INDIRECT_INDEXED, 5);

        // AND
        OPCODES[0x29] = new InstructionMetadata("AND", IMMEDIATE, 2);
        OPCODES[0x25] = new InstructionMetadata("AND", ZERO_PAGE, 3);
        OPCODES[0x35] = new InstructionMetadata("AND", ZERO_PAGE_X, 4);
        OPCODES[0x2D] = new InstructionMetadata("AND", ABSOLUTE, 4);
        OPCODES[0x3D] = new InstructionMetadata("AND", ABSOLUTE_X, 4);
        OPCODES[0x39] = new InstructionMetadata("AND", ABSOLUTE_Y, 4);
        OPCODES[0x21] = new InstructionMetadata("AND", INDEXED_INDIRECT, 6);
        OPCODES[0x31] = new InstructionMetadata("AND", INDIRECT_INDEXED, 5);

        // ASL
        OPCODES[0x0A] = new InstructionMetadata("ASL", ACCUMULATOR, 2);
        OPCODES[0x06] = new InstructionMetadata("ASL", ZERO_PAGE, 5);
        OPCODES[0x16] = new InstructionMetadata("ASL", ZERO_PAGE_X, 6);
        OPCODES[0x0E] = new InstructionMetadata("ASL", ABSOLUTE, 6);
        OPCODES[0x1E] = new InstructionMetadata("ASL", ABSOLUTE_X, 7);

        // BCC, BCS, BEQ, BMI, BNE, BPL, BVC, BVS
        OPCODES[0x90] = new InstructionMetadata("BCC", RELATIVE, 2);
        OPCODES[0xB0] = new InstructionMetadata("BCS", RELATIVE, 2);
        OPCODES[0xF0] = new InstructionMetadata("BEQ", RELATIVE, 2);
        OPCODES[0x30] = new InstructionMetadata("BMI", RELATIVE, 2);
        OPCODES[0xD0] = new InstructionMetadata("BNE", RELATIVE, 2);
        OPCODES[0x10] = new InstructionMetadata("BPL", RELATIVE, 2);
        OPCODES[0x50] = new InstructionMetadata("BVC", RELATIVE, 2);
        OPCODES[0x70] = new InstructionMetadata("BVS", RELATIVE, 2);

        // BIT
        OPCODES[0x24] = new InstructionMetadata("BIT", ZERO_PAGE, 3);
        OPCODES[0x2C] = new InstructionMetadata("BIT", ABSOLUTE, 4);

        // BRK
        OPCODES[0x00] = new InstructionMetadata("BRK", IMPLIED, 7);

        // CLC, CLD, CLI, CLV, SEC, SED, SEI
        OPCODES[0x18] = new InstructionMetadata("CLC", IMPLIED, 2);
        OPCODES[0xD8] = new InstructionMetadata("CLD", IMPLIED, 2);
        OPCODES[0x58] = new InstructionMetadata("CLI", IMPLIED, 2);
        OPCODES[0xB8] = new InstructionMetadata("CLV", IMPLIED, 2);
        OPCODES[0x38] = new InstructionMetadata("SEC", IMPLIED, 2);
        OPCODES[0xF8] = new InstructionMetadata("SED", IMPLIED, 2);
        OPCODES[0x78] = new InstructionMetadata("SEI", IMPLIED, 2);

        // CMP
        OPCODES[0xC9] = new InstructionMetadata("CMP", IMMEDIATE, 2);
        OPCODES[0xC5] = new InstructionMetadata("CMP", ZERO_PAGE, 3);
        OPCODES[0xD5] = new InstructionMetadata("CMP", ZERO_PAGE_X, 4);
        OPCODES[0xCD] = new InstructionMetadata("CMP", ABSOLUTE, 4);
        OPCODES[0xDD] = new InstructionMetadata("CMP", ABSOLUTE_X, 4);
        OPCODES[0xD9] = new InstructionMetadata("CMP", ABSOLUTE_Y, 4);
        OPCODES[0xC1] = new InstructionMetadata("CMP", INDEXED_INDIRECT, 6);
        OPCODES[0xD1] = new InstructionMetadata("CMP", INDIRECT_INDEXED, 5);

        // CPX
        OPCODES[0xE0] = new InstructionMetadata("CPX", IMMEDIATE, 2);
        OPCODES[0xE4] = new InstructionMetadata("CPX", ZERO_PAGE, 3);
        OPCODES[0xEC] = new InstructionMetadata("CPX", ABSOLUTE, 4);

        // CPY
        OPCODES[0xC0] = new InstructionMetadata("CPY", IMMEDIATE, 2);
        OPCODES[0xC4] = new InstructionMetadata("CPY", ZERO_PAGE, 3);
        OPCODES[0xCC] = new InstructionMetadata("CPY", ABSOLUTE, 4);

        // DEC
        OPCODES[0xC6] = new InstructionMetadata("DEC", ZERO_PAGE, 5);
        OPCODES[0xD6] = new InstructionMetadata("DEC", ZERO_PAGE_X, 6);
        OPCODES[0xCE] = new InstructionMetadata("DEC", ABSOLUTE, 6);
        OPCODES[0xDE] = new InstructionMetadata("DEC", ABSOLUTE_X, 7);

        // DEX, DEY, INX, INY
        OPCODES[0xCA] = new InstructionMetadata("DEX", IMPLIED, 2);
        OPCODES[0x88] = new InstructionMetadata("DEY", IMPLIED, 2);
        OPCODES[0xE8] = new InstructionMetadata("INX", IMPLIED, 2);
        OPCODES[0xC8] = new InstructionMetadata("INY", IMPLIED, 2);

        // EOR
        OPCODES[0x49] = new InstructionMetadata("EOR", IMMEDIATE, 2);
        OPCODES[0x45] = new InstructionMetadata("EOR", ZERO_PAGE, 3);
        OPCODES[0x55] = new InstructionMetadata("EOR", ZERO_PAGE_X, 4);
        OPCODES[0x4D] = new InstructionMetadata("EOR", ABSOLUTE, 4);
        OPCODES[0x5D] = new InstructionMetadata("EOR", ABSOLUTE_X, 4);
        OPCODES[0x59] = new InstructionMetadata("EOR", ABSOLUTE_Y, 4);
        OPCODES[0x41] = new InstructionMetadata("EOR", INDEXED_INDIRECT, 6);
        OPCODES[0x51] = new InstructionMetadata("EOR", INDIRECT_INDEXED, 5);

        // INC
        OPCODES[0xE6] = new InstructionMetadata("INC", ZERO_PAGE, 5);
        OPCODES[0xF6] = new InstructionMetadata("INC", ZERO_PAGE_X, 6);
        OPCODES[0xEE] = new InstructionMetadata("INC", ABSOLUTE, 6);
        OPCODES[0xFE] = new InstructionMetadata("INC", ABSOLUTE_X, 7);

        // JMP, JSR
        OPCODES[0x4C] = new InstructionMetadata("JMP", ABSOLUTE, 3);
        OPCODES[0x6C] = new InstructionMetadata("JMP", INDIRECT, 5);
        OPCODES[0x20] = new InstructionMetadata("JSR", ABSOLUTE, 6);

        // LDA
        OPCODES[0xA9] = new InstructionMetadata("LDA", IMMEDIATE, 2);
        OPCODES[0xA5] = new InstructionMetadata("LDA", ZERO_PAGE, 3);
        OPCODES[0xB5] = new InstructionMetadata("LDA", ZERO_PAGE_X, 4);
        OPCODES[0xAD] = new InstructionMetadata("LDA", ABSOLUTE, 4);
        OPCODES[0xBD] = new InstructionMetadata("LDA", ABSOLUTE_X, 4);
        OPCODES[0xB9] = new InstructionMetadata("LDA", ABSOLUTE_Y, 4);
        OPCODES[0xA1] = new InstructionMetadata("LDA", INDEXED_INDIRECT, 6);
        OPCODES[0xB1] = new InstructionMetadata("LDA", INDIRECT_INDEXED, 5);

        // LDX
        OPCODES[0xA2] = new InstructionMetadata("LDX", IMMEDIATE, 2);
        OPCODES[0xA6] = new InstructionMetadata("LDX", ZERO_PAGE, 3);
        OPCODES[0xB6] = new InstructionMetadata("LDX", ZERO_PAGE_Y, 4);
        OPCODES[0xAE] = new InstructionMetadata("LDX", ABSOLUTE, 4);
        OPCODES[0xBE] = new InstructionMetadata("LDX", ABSOLUTE_Y, 4);

        // LDY
        OPCODES[0xA0] = new InstructionMetadata("LDY", IMMEDIATE, 2);
        OPCODES[0xA4] = new InstructionMetadata("LDY", ZERO_PAGE, 3);
        OPCODES[0xB4] = new InstructionMetadata("LDY", ZERO_PAGE_X, 4);
        OPCODES[0xAC] = new InstructionMetadata("LDY", ABSOLUTE, 4);
        OPCODES[0xBC] = new InstructionMetadata("LDY", ABSOLUTE_X, 4);

        // LSR
        OPCODES[0x4A] = new InstructionMetadata("LSR", ACCUMULATOR, 2);
        OPCODES[0x46] = new InstructionMetadata("LSR", ZERO_PAGE, 5);
        OPCODES[0x56] = new InstructionMetadata("LSR", ZERO_PAGE_X, 6);
        OPCODES[0x4E] = new InstructionMetadata("LSR", ABSOLUTE, 6);
        OPCODES[0x5E] = new InstructionMetadata("LSR", ABSOLUTE_X, 7);

        // NOP
        OPCODES[0xEA] = new InstructionMetadata("NOP", IMPLIED, 2);

        // ORA
        OPCODES[0x09] = new InstructionMetadata("ORA", IMMEDIATE, 2);
        OPCODES[0x05] = new InstructionMetadata("ORA", ZERO_PAGE, 3);
        OPCODES[0x15] = new InstructionMetadata("ORA", ZERO_PAGE_X, 4);
        OPCODES[0x0D] = new InstructionMetadata("ORA", ABSOLUTE, 4);
        OPCODES[0x1D] = new InstructionMetadata("ORA", ABSOLUTE_X, 4);
        OPCODES[0x19] = new InstructionMetadata("ORA", ABSOLUTE_Y, 4);
        OPCODES[0x01] = new InstructionMetadata("ORA", INDEXED_INDIRECT, 6);
        OPCODES[0x11] = new InstructionMetadata("ORA", INDIRECT_INDEXED, 5);

        // PHA, PHP, PLA, PLP
        OPCODES[0x48] = new InstructionMetadata("PHA", IMPLIED, 3);
        OPCODES[0x08] = new InstructionMetadata("PHP", IMPLIED, 3);
        OPCODES[0x68] = new InstructionMetadata("PLA", IMPLIED, 4);
        OPCODES[0x28] = new InstructionMetadata("PLP", IMPLIED, 4);

        // ROL
        OPCODES[0x2A] = new InstructionMetadata("ROL", ACCUMULATOR, 2);
        OPCODES[0x26] = new InstructionMetadata("ROL", ZERO_PAGE, 5);
        OPCODES[0x36] = new InstructionMetadata("ROL", ZERO_PAGE_X, 6);
        OPCODES[0x2E] = new InstructionMetadata("ROL", ABSOLUTE, 6);
        OPCODES[0x3E] = new InstructionMetadata("ROL", ABSOLUTE_X, 7);

        // ROR
        OPCODES[0x6A] = new InstructionMetadata("ROR", ACCUMULATOR, 2);
        OPCODES[0x66] = new InstructionMetadata("ROR", ZERO_PAGE, 5);
        OPCODES[0x76] = new InstructionMetadata("ROR", ZERO_PAGE_X, 6);
        OPCODES[0x6E] = new InstructionMetadata("ROR", ABSOLUTE, 6);
        OPCODES[0x7E] = new InstructionMetadata("ROR", ABSOLUTE_X, 7);

        // RTI, RTS
        OPCODES[0x40] = new InstructionMetadata("RTI", IMPLIED, 6);
        OPCODES[0x60] = new InstructionMetadata("RTS", IMPLIED, 6);

        // SBC
        OPCODES[0xE9] = new InstructionMetadata("SBC", IMMEDIATE, 2);
        OPCODES[0xE5] = new InstructionMetadata("SBC", ZERO_PAGE, 3);
        OPCODES[0xF5] = new InstructionMetadata("SBC", ZERO_PAGE_X, 4);
        OPCODES[0xED] = new InstructionMetadata("SBC", ABSOLUTE, 4);
        OPCODES[0xFD] = new InstructionMetadata("SBC", ABSOLUTE_X, 4);
        OPCODES[0xF9] = new InstructionMetadata("SBC", ABSOLUTE_Y, 4);
        OPCODES[0xE1] = new InstructionMetadata("SBC", INDEXED_INDIRECT, 6);
        OPCODES[0xF1] = new InstructionMetadata("SBC", INDIRECT_INDEXED, 5);

        // STA
        OPCODES[0x85] = new InstructionMetadata("STA", ZERO_PAGE, 3);
        OPCODES[0x95] = new InstructionMetadata("STA", ZERO_PAGE_X, 4);
        OPCODES[0x8D] = new InstructionMetadata("STA", ABSOLUTE, 4);
        OPCODES[0x9D] = new InstructionMetadata("STA", ABSOLUTE_X, 5);
        OPCODES[0x99] = new InstructionMetadata("STA", ABSOLUTE_Y, 5);
        OPCODES[0x81] = new InstructionMetadata("STA", INDEXED_INDIRECT, 6);
        OPCODES[0x91] = new InstructionMetadata("STA", INDIRECT_INDEXED, 6);

        // STX, STY
        OPCODES[0x86] = new InstructionMetadata("STX", ZERO_PAGE, 3);
        OPCODES[0x96] = new InstructionMetadata("STX", ZERO_PAGE_Y, 4);
        OPCODES[0x8E] = new InstructionMetadata("STX", ABSOLUTE, 4);
        OPCODES[0x84] = new InstructionMetadata("STY", ZERO_PAGE, 3);
        OPCODES[0x94] = new InstructionMetadata("STY", ZERO_PAGE_X, 4);
        OPCODES[0x8C] = new InstructionMetadata("STY", ABSOLUTE, 4);

        // TAX, TAY, TSX, TXA, TXS, TYA
        OPCODES[0xAA] = new InstructionMetadata("TAX", IMPLIED, 2);
        OPCODES[0xA8] = new InstructionMetadata("TAY", IMPLIED, 2);
        OPCODES[0xBA] = new InstructionMetadata("TSX", IMPLIED, 2);
        OPCODES[0x8A] = new InstructionMetadata("TXA", IMPLIED, 2);
        OPCODES[0x9A] = new InstructionMetadata("TXS", IMPLIED, 2);
        OPCODES[0x98] = new InstructionMetadata("TYA", IMPLIED, 2);

        for (int i = 0; i < OPCODES.length; i++) {
            if (OPCODES[i] == null) {
                OPCODES[i] = new InstructionMetadata("???", UNKNOWN, 0);
            }
        }
    }
}

