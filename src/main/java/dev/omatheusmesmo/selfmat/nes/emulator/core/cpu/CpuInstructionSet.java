package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu;

import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.InstructionExecutor;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.InstructionMetadata;
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.Opcodes; // Needed for InstructionMetadata to reference Opcodes.getInstructionMetadata
import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.AddressingMode; // Needed for InstructionMetadata.addressingMode()

import java.util.HashMap;
import java.util.Map;

import static dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.CpuConstants.*;

/**
 * Encapsulates the execution logic for 6502 CPU instructions.
 * This class interacts directly with the CPU's state (registers, bus, flags)
 * through a provided CPU instance. It also handles the dispatching of instructions
 * based on their mnemonic.
 */
public class CpuInstructionSet {

    private final CPU cpu; // Reference to the CPU instance
    private final Map<String, InstructionExecutor> instructionExecutors = new HashMap<>();

    /**
     * Constructs a new CpuInstructionSet with a reference to the CPU instance.
     * @param cpu The CPU instance this instruction set will operate on.
     */
    public CpuInstructionSet(CPU cpu) {
        this.cpu = cpu;
        setupInstructionExecutors(); // Initialize the instruction dispatch map
    }

    /**
     * Updates the Zero (Z) and Negative (N) flags based on the given value.
     * @param value The value to check.
     */
    public void updateNegativeAndZeroFlags(int value) {
        cpu.setFlag(ZERO_FLAG, (value & BYTE_MASK) == INITIAL_VALUE);
        cpu.setFlag(NEGATIVE_FLAG, (value & NEGATIVE_FLAG) != INITIAL_VALUE);
    }

    /**
     * Updates the Carry (C), Zero (Z), Negative (N), and Overflow (V) flags
     * for arithmetic operations like ADC and SBC.
     */
    private void updateArithmeticFlags(int result, int operand1, int operand2, boolean isSBC) {
        // Carry Flag
        if (isSBC) {
            cpu.setFlag(CARRY_FLAG, (result >= 0)); // SBC: Carry set if no borrow (result >= 0)
        } else {
            cpu.setFlag(CARRY_FLAG, result > BYTE_MASK); // ADC: Carry set if overflow > 255
        }

        // Zero Flag
        cpu.setFlag(ZERO_FLAG, (result & BYTE_MASK) == INITIAL_VALUE);

        // Negative Flag
        cpu.setFlag(NEGATIVE_FLAG, (result & NEGATIVE_FLAG) != INITIAL_VALUE);

        // Overflow Flag
        // Formula: (Operand1 ^ Result) & (Operand2 ^ Result) & 0x80
        // ADC: Overflow if signs of operands are same, but result sign is different.
        // SBC: Operand2 is effectively inverted.
        int intermediate = (operand1 ^ result) & (operand2 ^ result);
        if (isSBC) {
            intermediate = (operand1 ^ result) & ((~operand2) ^ result);
        }
        cpu.setFlag(OVERFLOW_FLAG, (intermediate & NEGATIVE_FLAG) != INITIAL_VALUE);
    }

    // --- Load and Store ---

    public void lda(int operandAddress) {
        cpu.accumulator = cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
    }

    public void ldx(int operandAddress) {
        cpu.indexX = cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
    }

    public void ldy(int operandAddress) {
        cpu.indexY = cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
    }

    public void sta(int operandAddress) {
        cpu.write(operandAddress, (byte) (cpu.accumulator & BYTE_MASK));
    }

    public void stx(int operandAddress) {
        cpu.write(operandAddress, (byte) (cpu.indexX & BYTE_MASK));
    }

    public void sty(int operandAddress) {
        cpu.write(operandAddress, (byte) (cpu.indexY & BYTE_MASK));
    }

    // --- Increment and Decrement ---

    public void inx(int operandAddress) {
        cpu.indexX = (cpu.indexX + 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
    }

    public void dex(int operandAddress) {
        cpu.indexX = (cpu.indexX - 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
    }

    public void iny(int operandAddress) {
        cpu.indexY = (cpu.indexY + 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
    }

    public void dey(int operandAddress) {
        cpu.indexY = (cpu.indexY - 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
    }

    public void inc(int operandAddress) {
        int value = (cpu.read(operandAddress) + 1) & BYTE_MASK;
        cpu.write(operandAddress, (byte) value);
        updateNegativeAndZeroFlags(value);
    }

    public void dec(int operandAddress) {
        int value = (cpu.read(operandAddress) - 1) & BYTE_MASK;
        cpu.write(operandAddress, (byte) value);
        updateNegativeAndZeroFlags(value);
    }

    // --- Branches and Jumps ---

    public void jmp(int operandAddress) {
        cpu.programCounter = operandAddress;
    }

    public void jsr(int operandAddress) {
        // Push PC-1 (High then Low)
        int returnAddr = cpu.programCounter - 1;
        cpu.pushAddress(returnAddr); 
        cpu.programCounter = operandAddress;
    }

    public void rts(int operandAddress) {
        int returnAddr = cpu.popAddress();
        cpu.programCounter = returnAddr + 1;
    }

    public void brk(int operandAddress) {
        // Push PC+1 (High then Low)
        // BRK is a 2-byte instruction (usually padding byte after opcode), PC has already advanced by 1 in fetch
        cpu.pushAddress(cpu.programCounter); 
        
        // Push Status with Break (B) flag set
        cpu.push((byte) (cpu.statusRegister | BREAK_COMMAND | UNUSED_FLAG));
        
        cpu.setFlag(INTERRUPT_DISABLE, true);
        
        // Load Interrupt Vector (0xFFFE/F)
        int lo = cpu.read(0xFFFE) & BYTE_MASK;
        int hi = cpu.read(0xFFFF) & BYTE_MASK;
        cpu.programCounter = (hi << BITS_PER_BYTE) | lo;
    }

    // --- Arithmetic and Logic ---

    public void adc(int operandAddress) {
        int fetched = cpu.read(operandAddress) & BYTE_MASK;
        int sum = cpu.accumulator + fetched + (cpu.isFlagSet(CARRY_FLAG) ? 1 : 0);
        
        updateArithmeticFlags(sum, cpu.accumulator, fetched, false);
        cpu.accumulator = sum & BYTE_MASK;
    }

    public void sbc(int operandAddress) {
        int fetched = cpu.read(operandAddress) & BYTE_MASK;
        // SBC is A - M - (1 - C) -> A + (-M - 1) + C -> A + ~M + C
        int inverted = fetched ^ BYTE_MASK;
        int sum = cpu.accumulator + inverted + (cpu.isFlagSet(CARRY_FLAG) ? 1 : 0);
        
        updateArithmeticFlags(sum, cpu.accumulator, fetched, true);
        cpu.accumulator = sum & BYTE_MASK;
    }

    public void and(int operandAddress) {
        cpu.accumulator &= cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
    }

    public void ora(int operandAddress) {
        cpu.accumulator |= cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
    }

    public void eor(int operandAddress) {
        cpu.accumulator ^= cpu.read(operandAddress) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
    }

    // --- Stack Operations ---

    public void pha(int operandAddress) {
        cpu.push((byte) cpu.accumulator);
    }

    public void pla(int operandAddress) {
        cpu.accumulator = cpu.pop() & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
    }

    public void php(int operandAddress) {
        // PHP always pushes with Break (B) and Unused (U) flags set
        cpu.push((byte) (cpu.statusRegister | BREAK_COMMAND | UNUSED_FLAG));
    }

    public void plp(int operandAddress) {
        // Pull into status, but ignore Unused (U) and Break (B) bits from stack
        // U is always 1, B usually ignored/cleared in register
        int pulled = cpu.pop() & BYTE_MASK;
        cpu.statusRegister = pulled;
        cpu.setFlag(UNUSED_FLAG, true);
        cpu.setFlag(BREAK_COMMAND, false); // B flag does not physically exist in the register
    }

    // --- Dispatching Logic ---

    /**
     * Sets up the map of instruction mnemonics to their respective execution logic.
     * This method is called once during the construction of CpuInstructionSet.
     */
    private void setupInstructionExecutors() {
        // Load and Store
        instructionExecutors.put("LDA", this::lda);
        instructionExecutors.put("LDX", this::ldx);
        instructionExecutors.put("LDY", this::ldy);
        instructionExecutors.put("STA", this::sta);
        instructionExecutors.put("STX", this::stx);
        instructionExecutors.put("STY", this::sty);

        // Increment/Decrement
        instructionExecutors.put("INX", this::inx);
        instructionExecutors.put("DEX", this::dex);
        instructionExecutors.put("INY", this::iny);
        instructionExecutors.put("DEY", this::dey);
        instructionExecutors.put("INC", this::inc);
        instructionExecutors.put("DEC", this::dec);

        // Branches and Jumps
        instructionExecutors.put("JMP", this::jmp);
        instructionExecutors.put("JSR", this::jsr);
        instructionExecutors.put("RTS", this::rts);
        instructionExecutors.put("BRK", this::brk);

        // Arithmetic and Logic
        instructionExecutors.put("ADC", this::adc);
        instructionExecutors.put("SBC", this::sbc);
        instructionExecutors.put("AND", this::and);
        instructionExecutors.put("ORA", this::ora);
        instructionExecutors.put("EOR", this::eor);

        // Stack Operations
        instructionExecutors.put("PHA", this::pha);
        instructionExecutors.put("PLA", this::pla);
        instructionExecutors.put("PHP", this::php);
        instructionExecutors.put("PLP", this::plp);
    }

    /**
     * Executes the instruction defined by the metadata.
     * Dispatches the call to the appropriate instruction implementation based on the mnemonic.
     * @param metadata The InstructionMetadata containing the mnemonic.
     * @param operandAddress The resolved memory address for the operand.
     * @throws IllegalStateException if an executor for the mnemonic is not found.
     */
    public void execute(InstructionMetadata metadata, int operandAddress) {
        InstructionExecutor executor = instructionExecutors.get(metadata.mnemonic());
        if (executor == null) {
            throw new IllegalStateException("Executor not found for instruction: " + metadata.mnemonic() + " (Opcode: " + metadata.cycles() + ")");
        }
        executor.execute(operandAddress);
    }
}
