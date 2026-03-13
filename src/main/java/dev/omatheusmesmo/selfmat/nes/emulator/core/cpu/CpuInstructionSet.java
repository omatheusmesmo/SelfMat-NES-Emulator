package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu;

import dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode.*;

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
     * Executes the instruction defined by the metadata.
     * Dispatches the call to the appropriate instruction implementation based on the mnemonic.
     * @param metadata The InstructionMetadata containing the mnemonic.
     * @param operand The resolved operand containing address and page crossing info.
     * @return The number of additional CPU cycles consumed.
     * @throws IllegalStateException if an executor for the mnemonic is not found.
     */
    public int execute(InstructionMetadata metadata, Operand operand) {
        InstructionExecutor executor = instructionExecutors.get(metadata.mnemonic());
        if (executor == null) {
            throw new IllegalStateException("Executor not found for instruction: " + metadata.mnemonic());
        }
        return executor.execute(operand);
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

    public int lda(Operand op) {
        cpu.accumulator = cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return op.pageCrossed() ? 1 : 0; // LDA gains 1 cycle if page is crossed
    }

    public int ldx(Operand op) {
        cpu.indexX = cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
        return op.pageCrossed() ? 1 : 0;
    }

    public int ldy(Operand op) {
        cpu.indexY = cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
        return op.pageCrossed() ? 1 : 0;
    }

    public int sta(Operand op) {
        cpu.write(op.address(), (byte) (cpu.accumulator & BYTE_MASK));
        return 0; // STA never adds cycles for page crossing in indexed modes
    }

    public int stx(Operand op) {
        cpu.write(op.address(), (byte) (cpu.indexX & BYTE_MASK));
        return 0;
    }

    public int sty(Operand op) {
        cpu.write(op.address(), (byte) (cpu.indexY & BYTE_MASK));
        return 0;
    }

    // --- Increment and Decrement ---

    public int inx(Operand op) {
        cpu.indexX = (cpu.indexX + 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
        return 0;
    }

    public int dex(Operand op) {
        cpu.indexX = (cpu.indexX - 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexX);
        return 0;
    }

    public int iny(Operand op) {
        cpu.indexY = (cpu.indexY + 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
        return 0;
    }

    public int dey(Operand op) {
        cpu.indexY = (cpu.indexY - 1) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.indexY);
        return 0;
    }

    public int inc(Operand op) {
        int value = (cpu.read(op.address()) + 1) & BYTE_MASK;
        cpu.write(op.address(), (byte) value);
        updateNegativeAndZeroFlags(value);
        return 0;
    }

    public int dec(Operand op) {
        int value = (cpu.read(op.address()) - 1) & BYTE_MASK;
        cpu.write(op.address(), (byte) value);
        updateNegativeAndZeroFlags(value);
        return 0;
    }

    // --- Branches and Jumps ---

    public int jmp(Operand op) {
        cpu.programCounter = op.address();
        return 0;
    }

    public int jsr(Operand op) {
        // Push PC-1 (High then Low)
        int returnAddr = cpu.programCounter - 1;
        cpu.pushAddress(returnAddr); 
        cpu.programCounter = op.address();
        return 0;
    }

    public int rts(Operand op) {
        int returnAddr = cpu.popAddress();
        cpu.programCounter = returnAddr + 1;
        return 0;
    }

    public int brk(Operand op) {
        // Push PC+1 (High then Low)
        // BRK is a 2-byte instruction (usually padding byte after opcode), PC has already advanced by 1 in fetch
        cpu.pushAddress(cpu.programCounter); 
        
        // Push Status with Break (B) flag set
        cpu.push((byte) (cpu.statusRegister | BREAK_COMMAND | UNUSED_FLAG));
        
        cpu.setFlag(INTERRUPT_DISABLE, true);
        
        // Load Interrupt Vector (0xFFFE/F)
        int lo = cpu.read(INTERRUPT_VECTOR_LOW) & BYTE_MASK;
        int hi = cpu.read(INTERRUPT_VECTOR_HIGH) & BYTE_MASK;
        cpu.programCounter = (hi << BITS_PER_BYTE) | lo;
        return 0;
    }

    // --- Arithmetic and Logic ---

    public int adc(Operand op) {
        int fetched = cpu.read(op.address()) & BYTE_MASK;
        int sum = cpu.accumulator + fetched + (cpu.isFlagSet(CARRY_FLAG) ? 1 : 0);
        
        updateArithmeticFlags(sum, cpu.accumulator, fetched, false);
        cpu.accumulator = sum & BYTE_MASK;
        return op.pageCrossed() ? 1 : 0;
    }

    public int sbc(Operand op) {
        int fetched = cpu.read(op.address()) & BYTE_MASK;
        // SBC is A - M - (1 - C) -> A + (-M - 1) + C -> A + ~M + C
        int inverted = fetched ^ BYTE_MASK;
        int sum = cpu.accumulator + inverted + (cpu.isFlagSet(CARRY_FLAG) ? 1 : 0);
        
        updateArithmeticFlags(sum, cpu.accumulator, fetched, true);
        cpu.accumulator = sum & BYTE_MASK;
        return op.pageCrossed() ? 1 : 0;
    }

    public int and(Operand op) {
        cpu.accumulator &= cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return op.pageCrossed() ? 1 : 0;
    }

    public int ora(Operand op) {
        cpu.accumulator |= cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return op.pageCrossed() ? 1 : 0;
    }

    public int eor(Operand op) {
        cpu.accumulator ^= cpu.read(op.address()) & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return op.pageCrossed() ? 1 : 0;
    }

    // --- Stack Operations ---

    public int pha(Operand op) {
        cpu.push((byte) cpu.accumulator);
        return 0;
    }

    public int pla(Operand op) {
        cpu.accumulator = cpu.pop() & BYTE_MASK;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return 0;
    }

    public int php(Operand op) {
        // PHP always pushes with Break (B) and Unused (U) flags set
        cpu.push((byte) (cpu.statusRegister | BREAK_COMMAND | UNUSED_FLAG));
        return 0;
    }

    public int plp(Operand op) {
        // Pull into status, but ignore Unused (U) and Break (B) bits from stack
        // U is always 1, B usually ignored/cleared in register
        int pulled = cpu.pop() & BYTE_MASK;
        cpu.statusRegister = pulled;
        cpu.setFlag(UNUSED_FLAG, true);
        cpu.setFlag(BREAK_COMMAND, false); // B flag does not physically exist in the register
        return 0;
    }

    // Handle Processor Flags

    public int clc(Operand op) {
        cpu.setFlag(CARRY_FLAG, false);
        return 0;
    }

    public int sec(Operand op) {
        cpu.setFlag(CARRY_FLAG, true);
        return 0;
    }

    public int cld(Operand op) {
        cpu.setFlag(DECIMAL_MODE, false);
        return 0;
    }

    public int sed(Operand op) {
        cpu.setFlag(DECIMAL_MODE, true);
        return 0;
    }

    public int cli(Operand op) {
        cpu.setFlag(INTERRUPT_DISABLE, false);
        return 0;
    }

    public int sei(Operand op) {
        cpu.setFlag(INTERRUPT_DISABLE, true);
        return 0;
    }

    public int clv(Operand op) {
        cpu.setFlag(OVERFLOW_FLAG, false);
        return 0;
    }

    public int cmp(Operand op) {
        int fetched = cpu.read(op.address()) & BYTE_MASK;
        int result = cpu.accumulator - fetched;

        // Set flags based on the comparison
        cpu.setFlag(CARRY_FLAG, cpu.accumulator >= fetched);
        updateNegativeAndZeroFlags(result);
        return op.pageCrossed() ? 1 : 0;
    }

    public int cpx(Operand op) {
        int fetched = cpu.read(op.address()) & BYTE_MASK;
        int result = cpu.indexX - fetched;

        // Set flags based on the comparison
        cpu.setFlag(CARRY_FLAG, cpu.indexX >= fetched);
        updateNegativeAndZeroFlags(result);
        return 0;
    }

    public int cpy(Operand op) {
        int fetched = cpu.read(op.address()) & BYTE_MASK;
        int result = cpu.indexY - fetched;

        // Set flags based on the comparison
        cpu.setFlag(CARRY_FLAG, cpu.indexY >= fetched);
        updateNegativeAndZeroFlags(result);
        return 0;
    }

    public int tax(Operand op) {
        cpu.indexX = cpu.accumulator;
        updateNegativeAndZeroFlags(cpu.indexX);
        return 0;
    }

    public int tay(Operand op) {
        cpu.indexY = cpu.accumulator;
        updateNegativeAndZeroFlags(cpu.indexY);
        return 0;
    }

    public int tsx(Operand op) {
        cpu.indexX = cpu.stackPointer;
        updateNegativeAndZeroFlags(cpu.indexX);
        return 0;
    }

    public int txa(Operand op) {
        cpu.accumulator = cpu.indexX;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return 0;
    }

    public int tya(Operand op) {
        cpu.accumulator = cpu.indexY;
        updateNegativeAndZeroFlags(cpu.accumulator);
        return 0;
    }

    public int txs(Operand op) {
        cpu.stackPointer = cpu.indexX;
        return 0;
    }

    public int bit(Operand op) {
        int value = cpu.read(op.address()) & BYTE_MASK;
        cpu.setFlag(NEGATIVE_FLAG, (value & NEGATIVE_FLAG) != INITIAL_VALUE);
        cpu.setFlag(OVERFLOW_FLAG, (value & OVERFLOW_FLAG) != INITIAL_VALUE); // V flag is bit 6 of the operand
        cpu.setFlag(ZERO_FLAG, (cpu.accumulator & value) == INITIAL_VALUE);
        return 0;
    }

    public int asl(Operand op) {
        if (op.address() == ACCUMULATOR_SENTINEL) {
            cpu.setFlag(CARRY_FLAG, (cpu.accumulator & NEGATIVE_FLAG) != INITIAL_VALUE);
            cpu.accumulator = (cpu.accumulator << 1) & BYTE_MASK;
            updateNegativeAndZeroFlags(cpu.accumulator);
        } else {
            int value = cpu.read(op.address()) & BYTE_MASK;
            cpu.setFlag(CARRY_FLAG, (value & NEGATIVE_FLAG) != INITIAL_VALUE);
            value = (value << 1) & BYTE_MASK;
            cpu.write(op.address(), (byte) value);
            updateNegativeAndZeroFlags(value);
        }
        return 0;
    }

    public int lsr(Operand op) {
        if (op.address() == ACCUMULATOR_SENTINEL) {
            cpu.setFlag(CARRY_FLAG, (cpu.accumulator & CARRY_FLAG) != INITIAL_VALUE);
            cpu.accumulator = (cpu.accumulator >> 1) & BYTE_MASK;
            updateNegativeAndZeroFlags(cpu.accumulator);
        } else {
            int value = cpu.read(op.address()) & BYTE_MASK;
            cpu.setFlag(CARRY_FLAG, (value & CARRY_FLAG) != INITIAL_VALUE);
            value = (value >> 1) & BYTE_MASK;
            cpu.write(op.address(), (byte) value);
            updateNegativeAndZeroFlags(value);
        }
        return 0;
    }

    public int rol(Operand op) {
        boolean oldCarry = cpu.isFlagSet(CARRY_FLAG);
        if (op.address() == ACCUMULATOR_SENTINEL) {
            cpu.setFlag(CARRY_FLAG, (cpu.accumulator & NEGATIVE_FLAG) != INITIAL_VALUE);
            cpu.accumulator = ((cpu.accumulator << 1) | (oldCarry ? 1 : 0)) & BYTE_MASK;
            updateNegativeAndZeroFlags(cpu.accumulator);
        } else {
            int value = cpu.read(op.address()) & BYTE_MASK;
            cpu.setFlag(CARRY_FLAG, (value & NEGATIVE_FLAG) != INITIAL_VALUE);
            value = ((value << 1) | (oldCarry ? 1 : 0)) & BYTE_MASK;
            cpu.write(op.address(), (byte) value);
            updateNegativeAndZeroFlags(value);
        }
        return 0;
    }

    public int ror(Operand op) {
        boolean oldCarry = cpu.isFlagSet(CARRY_FLAG);
        if (op.address() == ACCUMULATOR_SENTINEL) {
            cpu.setFlag(CARRY_FLAG, (cpu.accumulator & CARRY_FLAG) != INITIAL_VALUE);
            cpu.accumulator = ((cpu.accumulator >> 1) | (oldCarry ? NEGATIVE_FLAG : 0)) & BYTE_MASK;
            updateNegativeAndZeroFlags(cpu.accumulator);
        } else {
            int value = cpu.read(op.address()) & BYTE_MASK;
            cpu.setFlag(CARRY_FLAG, (value & CARRY_FLAG) != INITIAL_VALUE);
            value = ((value >> 1) | (oldCarry ? NEGATIVE_FLAG : 0)) & BYTE_MASK;
            cpu.write(op.address(), (byte) value);
            updateNegativeAndZeroFlags(value);
        }
        return 0;
    }

    public int rti(Operand op) {
        // Pull Status first (but ignore B and U bits)
        int pulledStatus = cpu.pop() & BYTE_MASK;
        cpu.statusRegister = pulledStatus;
        cpu.setFlag(UNUSED_FLAG, true);
        cpu.setFlag(BREAK_COMMAND, false); // B flag does not physically exist in the register

        // Then pull PC (Low then High)
        cpu.programCounter = cpu.popAddress();
        return 0;
    }

    public int nop(Operand op) {
        // No operation - do nothing
        return 0;
    }

    private int branchIf(boolean condition, Operand op){
        if (condition) {
            int extraCycles = 1;
            if (op.pageCrossed()) {
                extraCycles++;
            }
            cpu.programCounter = op.address();
            return extraCycles;
        }
        return 0;
    }

    public int bne(Operand op) {
        return branchIf(!cpu.isFlagSet(ZERO_FLAG), op);
    }

    public int beq(Operand op) {
        return branchIf(cpu.isFlagSet(ZERO_FLAG), op);
    }

    public int bpl(Operand op) {
        return branchIf(!cpu.isFlagSet(NEGATIVE_FLAG), op);
    }

    public int bmi(Operand op) {
        return branchIf(cpu.isFlagSet(NEGATIVE_FLAG), op);
    }

    public int bvc(Operand op) {
        return branchIf(!cpu.isFlagSet(OVERFLOW_FLAG), op);
    }

    public int bvs(Operand op) {
        return branchIf(cpu.isFlagSet(OVERFLOW_FLAG), op);
    }

    public int bcc(Operand op) {
        return branchIf(!cpu.isFlagSet(CARRY_FLAG), op);
    }

    public int bcs(Operand op) {
        return branchIf(cpu.isFlagSet(CARRY_FLAG), op);
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

        // Status Flag Changes
        instructionExecutors.put("CLC", this::clc);
        instructionExecutors.put("SEC", this::sec);
        instructionExecutors.put("CLD", this::cld);
        instructionExecutors.put("SED", this::sed);
        instructionExecutors.put("CLI", this::cli);
        instructionExecutors.put("SEI", this::sei);
        instructionExecutors.put("CLV", this::clv);

        // Comparisons
        instructionExecutors.put("CMP", this::cmp);
        instructionExecutors.put("CPX", this::cpx);
        instructionExecutors.put("CPY", this::cpy);

        // Register Transfers
        instructionExecutors.put("TAX", this::tax);
        instructionExecutors.put("TAY", this::tay);
        instructionExecutors.put("TSX", this::tsx);
        instructionExecutors.put("TXA", this::txa);
        instructionExecutors.put("TYA", this::tya);
        instructionExecutors.put("TXS", this::txs);

        // Bit Test
        instructionExecutors.put("BIT", this::bit);

        // Shifts and Rotates
        instructionExecutors.put("ASL", this::asl);
        instructionExecutors.put("LSR", this::lsr);
        instructionExecutors.put("ROL", this::rol);
        instructionExecutors.put("ROR", this::ror);

        // Return from Interrupt
        instructionExecutors.put("RTI", this::rti);

        // No Operation
        instructionExecutors.put("NOP", this::nop);

        // Branch Instructions
        instructionExecutors.put("BNE", this::bne);
        instructionExecutors.put("BEQ", this::beq);
        instructionExecutors.put("BPL", this::bpl);
        instructionExecutors.put("BMI", this::bmi);
        instructionExecutors.put("BVC", this::bvc);
        instructionExecutors.put("BVS", this::bvs);
        instructionExecutors.put("BCC", this::bcc);
        instructionExecutors.put("BCS", this::bcs);
    }
}
