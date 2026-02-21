package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu;


import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.AddressingMode;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.InstructionMetadata;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.Opcodes;
import dev.omatheusmesmo.selfmat.nes.emulator.core.memory.Bus;

public class CPU {

    private final Bus bus;

    private int accumulator;
    private int indexX;
    private int indexY;
    private int programCounter;
    private int stackPointer;
    private int statusRegister;
    private int cyclesRemaining;

    public static final int CARRY_FLAG          = (1 << 0);
    public static final int ZERO_FLAG           = (1 << 1);
    public static final int INTERRUPT_DISABLE   = (1 << 2);
    public static final int DECIMAL_MODE        = (1 << 3);
    public static final int BREAK_COMMAND       = (1 << 4);
    public static final int UNUSED_FLAG         = (1 << 5);
    public static final int OVERFLOW_FLAG       = (1 << 6);
    public static final int NEGATIVE_FLAG       = (1 << 7);
    public static final int RESET_VECTOR_LOW = 0xFFFC;
    public static final int RESET_VECTOR_HIGH = RESET_VECTOR_LOW + 1;
    public static final int INITIAL_STACK_POINTER = 0xFD;
    public static final int BYTE_MASK = 0xFF;
    public static final int BITS_PER_BYTE = 8;
    public static final int INITIAL_VALUE = 0;
    public static final int MAX_ADDRESS_VALUE = 0xFFFF;
    public static final int SIGNED_BYTE_MAX = 0x7F;
    public static final int BYTE_WRAP = 0x100;

    public CPU(Bus bus) {
        this.bus = bus;
        reset();
    }

    /**
     * Resets the CPU to its initial state.
     * Fetches the starting Program Counter from the Reset Vector (0xFFFC).
     */
    public void reset() {
        accumulator = INITIAL_VALUE;
        indexX = INITIAL_VALUE;
        indexY = INITIAL_VALUE;
        stackPointer = INITIAL_STACK_POINTER;
        statusRegister = INTERRUPT_DISABLE | UNUSED_FLAG;

        // Reset vector: read 16-bit address from 0xFFFC
        int lo = read(RESET_VECTOR_LOW) & BYTE_MASK;
        int hi = read(RESET_VECTOR_HIGH) & BYTE_MASK;
        programCounter = (hi << BITS_PER_BYTE) | lo;
    }

    private byte read(int address) {
        return bus.read(address);
    }

    private void write(int address, byte data) {
        bus.write(address, data);
    }

    private void setFlag(int flag, boolean value) {
        if (value) statusRegister |= flag;
        else statusRegister &= ~flag;
    }

    private boolean isFlagSet(int flag) {
        return (statusRegister & flag) != INITIAL_VALUE;
    }

    public void clock() {
        if (cyclesRemaining == INITIAL_VALUE) {
            cyclesRemaining = step();
        }
        cyclesRemaining--;
    }

    public int step() {
        int opcode = fetch() & BYTE_MASK;
        InstructionMetadata metadata = Opcodes.getInstructionMetadata(opcode);

        if (metadata.addressingMode() == AddressingMode.UNKNOWN) {
            throw new IllegalStateException(String.format("Unknown opcode: 0x%02X at PC: 0x%04X", opcode, programCounter - 1));
        }

        int address = resolveAddress(metadata.addressingMode());

        return metadata.cycles();
    }

    public byte fetch() {
        byte data = read(programCounter);
        programCounter= (programCounter + 1) & MAX_ADDRESS_VALUE; // Wrap around 16-bit address space
        return data;
    }

    private int resolveAddress(AddressingMode addressingMode) {
        return switch (addressingMode) {
            case IMMEDIATE -> {
                int address = programCounter;
                programCounter = (programCounter + 1) & MAX_ADDRESS_VALUE;
                yield address;
            }
            case ZERO_PAGE -> {
                yield fetch() & BYTE_MASK;
            }
            case ABSOLUTE -> {
                int lo = fetch() & BYTE_MASK;
                int hi = fetch() & BYTE_MASK;
                yield (hi << BITS_PER_BYTE) | lo;
            }
            case ZERO_PAGE_X -> {
                int base = fetch() & BYTE_MASK;
                yield (base + indexX) & BYTE_MASK;
            }
            case ZERO_PAGE_Y -> {
                int base = fetch() & BYTE_MASK;
                yield (base + indexY) & BYTE_MASK;
            }
            case ABSOLUTE_X -> {
                int lo = fetch() & BYTE_MASK;
                int hi = fetch() & BYTE_MASK;
                yield (((hi << BITS_PER_BYTE) | lo) + indexX) & MAX_ADDRESS_VALUE;
            }
            case  ABSOLUTE_Y -> {
                int lo = fetch() & BYTE_MASK;
                int hi = fetch() & BYTE_MASK;
                yield (((hi << BITS_PER_BYTE) | lo) + indexY) & MAX_ADDRESS_VALUE;
            }
            case RELATIVE -> {
                int offset = fetch() & BYTE_MASK;
                if (offset > SIGNED_BYTE_MAX) offset -= BYTE_WRAP; // sign-extend 8-bit offset
                yield (programCounter + offset) & MAX_ADDRESS_VALUE;
            }
            case INDIRECT -> {
                int pointerLo = fetch() & BYTE_MASK;
                int pointerHi = fetch() & BYTE_MASK;
                int pointer = (pointerHi << BITS_PER_BYTE) | pointerLo;
                // 6502 bug: if the low byte of the pointer is 0xFF, it wraps around to the start of the page
                int effectiveLo = read(pointer) & BYTE_MASK;
                int effectiveHi = read((pointer & BYTE_MASK) | ((pointer + 1) & BYTE_MASK)) & BYTE_MASK;
                yield (effectiveHi << BITS_PER_BYTE) | effectiveLo;
            }
            case  INDIRECT_INDEXED -> {
                int pointer = fetch() & BYTE_MASK;
                int baseLo = read(pointer) & BYTE_MASK;
                int baseHi = read(pointer + 1) & BYTE_MASK;
                int baseAddress = (baseHi << BITS_PER_BYTE) | baseLo;
                yield (baseAddress + indexY) & MAX_ADDRESS_VALUE;
            }
            case INDEXED_INDIRECT -> {
                int pointer = (fetch() + indexX) & BYTE_MASK;
                int effectiveLo = read(pointer) & BYTE_MASK;
                int effectiveHi = read(pointer + 1) & BYTE_MASK;
                yield (effectiveHi << BITS_PER_BYTE) | effectiveLo;
            }
            // IMPLIED, ACCUMULATOR etc. would be handled in instruction execution logic
            default -> INITIAL_VALUE;
        };


    }


}