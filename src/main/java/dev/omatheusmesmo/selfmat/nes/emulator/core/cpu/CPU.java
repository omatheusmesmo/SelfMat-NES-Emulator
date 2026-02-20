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

        return metadata.cycles();
    }

    public byte fetch() {
        byte data = read(programCounter);
        programCounter= (programCounter + 1) & MAX_ADDRESS_VALUE; // Wrap around 16-bit address space
        return data;
    }
}