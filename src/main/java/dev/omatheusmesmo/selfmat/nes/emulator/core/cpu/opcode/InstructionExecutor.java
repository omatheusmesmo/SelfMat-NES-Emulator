package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu.opcode;

@FunctionalInterface
public interface InstructionExecutor {

    void execute(int operandAddress);
}
