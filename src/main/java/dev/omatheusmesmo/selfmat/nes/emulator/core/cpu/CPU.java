package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu;

import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.AddressingMode;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.InstructionMetadata;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.Opcodes;
import dev.omatheusmesmo.selfmat.nes.emulator.core.memory.Bus;

import static dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.CpuConstants.*;

/**
 * Represents the 6502 Central Processing Unit (CPU) of the NES.
 * This class handles CPU state, fetches and executes instructions,
 * and manages interaction with the system bus.
 */
public class CPU {

    private final Bus bus;
    private final CpuInstructionSet instructionSet;

    // --- Registers ---
    /** Accumulator register (8-bit). */
    int accumulator; 
    /** X index register (8-bit). */
    int indexX;      
    /** Y index register (8-bit). */
    int indexY;      
    /** Program Counter (16-bit). Points to the next instruction to be fetched. */
    int programCounter;
    /** Stack Pointer (8-bit). Points to the next free location on the stack (page 0x01). */
    int stackPointer;
    /** Processor Status register (8-bit). Contains various flags. */
    int statusRegister;

    private int cyclesRemaining;

    /**
     * Constructs a new CPU instance.
     * @param bus The system bus that the CPU interacts with for memory access.
     */
    public CPU(Bus bus) {
        this.bus = bus;
        this.instructionSet = new CpuInstructionSet(this); // Inject this CPU instance into its instruction set
        reset();
    }

    // --- Control Methods ---

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

    /**
     * Simulates one clock cycle of the CPU.
     * Fetches and executes a new instruction if no cycles are remaining from the previous one.
     */
    public void clock() {
        if (cyclesRemaining == INITIAL_VALUE) {
            cyclesRemaining = step();
        }
        cyclesRemaining--;
    }

    /**
     * Executes a single 6502 instruction.
     * This involves fetching the opcode, decoding it, resolving the operand address,
     * and dispatching to the appropriate instruction execution logic.
     * @return The number of CPU cycles consumed by the executed instruction.
     * @throws IllegalStateException if an unknown or unimplemented opcode is encountered.
     */
    public int step() {
        int opcode = fetch() & BYTE_MASK;
        InstructionMetadata metadata = Opcodes.getInstructionMetadata(opcode);

        if (metadata.addressingMode() == AddressingMode.UNKNOWN) {
            throw new IllegalStateException(String.format("Unknown opcode: 0x%02X at PC: 0x%04X", opcode, (programCounter - 1) & MAX_ADDRESS_VALUE));
        }

        int operandAddress = resolveAddress(metadata.addressingMode());
        instructionSet.execute(metadata, operandAddress); // Delegates execution to CpuInstructionSet which has the dispatcher

        return metadata.cycles();
    }

    // --- Helper Methods for Memory and Flags (Package-Private for CpuInstructionSet) ---

    /**
     * Reads a byte from the system bus at the specified 16-bit address.
     * @param address The 16-bit memory address to read from.
     * @return The byte read from memory.
     */
    byte read(int address) { 
        return bus.read(address);
    }

    /**
     * Writes a byte to the system bus at the specified 16-bit address.
     * @param address The 16-bit memory address to write to.
     * @param data The byte data to write.
     */
    void write(int address, byte data) { 
        bus.write(address, data);
    }

    /**
     * Sets or clears a specific flag in the Processor Status register.
     * @param flag The flag bit to modify (e.g., CARRY_FLAG, ZERO_FLAG).
     * @param value True to set the flag, false to clear it.
     */
    void setFlag(int flag, boolean value) { 
        if (value) statusRegister |= flag;
        else statusRegister &= ~flag;
    }

    /**
     * Checks if a specific flag in the Processor Status register is set.
     * @param flag The flag bit to check.
     * @return True if the flag is set, false otherwise.
     */
    boolean isFlagSet(int flag) { 
        return (statusRegister & flag) != INITIAL_VALUE;
    }

    /**
     * Fetches the byte at the current Program Counter (PC) and increments the PC.
     * @return The byte fetched from memory.
     */
    byte fetch() {
        byte data = read(programCounter);
        programCounter = (programCounter + 1) & MAX_ADDRESS_VALUE;
        return data;
    }

    /**
     * Pushes a byte onto the CPU stack.
     * Decrements the stack pointer after writing the data.
     * The stack is located on memory page 0x01 ($0100 - $01FF).
     * @param data The byte to push onto the stack.
     */
    void push(byte data) { // Package-private for CpuInstructionSet
        write(0x0100 + stackPointer, data);
        stackPointer = (stackPointer - 1) & BYTE_MASK;
    }

    /**
     * Pushes a 16-bit address onto the CPU stack.
     * Pushes high byte then low byte.
     * @param address The 16-bit address to push.
     */
    void pushAddress(int address) { // Package-private for CpuInstructionSet
        push((byte) ((address >> BITS_PER_BYTE) & BYTE_MASK)); // Push high byte
        push((byte) (address & BYTE_MASK)); // Push low byte
    }

    /**
     * Pops a byte from the CPU stack.
     * Increments the stack pointer before reading the data.
     * The stack is located on memory page 0x01 ($0100 - $01FF).
     * @return The byte popped from the stack.
     */
    byte pop() { // Package-private for CpuInstructionSet
        stackPointer = (stackPointer + 1) & BYTE_MASK;
        return read(0x0100 + stackPointer);
    }

    /**
     * Pops a 16-bit address from the CPU stack.
     * Pops low byte then high byte.
     * @return The 16-bit address popped from the stack.
     */
    int popAddress() { // Package-private for CpuInstructionSet
        int lo = pop() & BYTE_MASK; // Pop low byte
        int hi = pop() & BYTE_MASK; // Pop high byte
        return (hi << BITS_PER_BYTE) | lo;
    }

    // --- Addressing Modes Resolution ---

    /**
     * Resolves the effective memory address for the current instruction's operand
     * based on the specified addressing mode. This method also advances the
     * Program Counter as necessary for multi-byte operands.
     * @param addressingMode The addressing mode to use.
     * @return The resolved 16-bit effective memory address of the operand.
     */
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
            case ABSOLUTE_Y -> {
                int lo = fetch() & BYTE_MASK;
                int hi = fetch() & BYTE_MASK;
                yield (((hi << BITS_PER_BYTE) | lo) + indexY) & MAX_ADDRESS_VALUE;
            }
            case RELATIVE -> {
                int offset = fetch() & BYTE_MASK;
                if (offset > SIGNED_BYTE_MAX) offset -= BYTE_WRAP;
                yield (programCounter + offset) & MAX_ADDRESS_VALUE;
            }
            case INDIRECT -> {
                int pointerLo = fetch() & BYTE_MASK;
                int pointerHi = fetch() & BYTE_MASK;
                int pointer = (pointerHi << BITS_PER_BYTE) | pointerLo;
                int effectiveLo = read(pointer) & BYTE_MASK;
                // Emulates 6502 JMP Indirect Page Boundary Bug:
                // if the low byte of the pointer is 0xFF, the high byte is fetched from the start of the current page.
                int effectiveHi = read((pointer & 0xFF00) | ((pointer + 1) & BYTE_MASK)) & BYTE_MASK;
                yield (effectiveHi << BITS_PER_BYTE) | effectiveLo;
            }
            case INDIRECT_INDEXED -> {
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
            // IMPLIED, ACCUMULATOR modes are handled by the instruction logic and do not require an operand address.
            default -> INITIAL_VALUE;
        };
    }
}
