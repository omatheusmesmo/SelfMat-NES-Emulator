package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

@FunctionalInterface
public interface InstructionExecutor {

    /**
     * Executes the instruction logic.
     * @param operand The resolved operand containing address and page crossing info.
     * @return The number of additional CPU cycles consumed (e.g., branch taken, page crossing).
     */
    int execute(Operand operand);
}
