package dev.omatheusmesmo.selfmat.nes.emulator.core.cpu;

/**
 * Utility class holding various constants for the 6502 CPU emulation.
 * These constants define flag bits, memory addresses, and bitwise masks.
 */
public final class CpuConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CpuConstants() {
        // Utility class
    }

    // --- Processor Status (P) Register Flags ---
    /** Carry Flag (C) - Bit 0 */
    public static final int CARRY_FLAG          = (1 << 0);
    /** Zero Flag (Z) - Bit 1 */
    public static final int ZERO_FLAG           = (1 << 1);
    /** Interrupt Disable Flag (I) - Bit 2 */
    public static final int INTERRUPT_DISABLE   = (1 << 2);
    /** Decimal Mode Flag (D) - Bit 3 */
    public static final int DECIMAL_MODE        = (1 << 3);
    /** Break Command Flag (B) - Bit 4 */
    public static final int BREAK_COMMAND       = (1 << 4);
    /** Unused Flag (U) - Bit 5 (Always set to 1) */
    public static final int UNUSED_FLAG         = (1 << 5);
    /** Overflow Flag (V) - Bit 6 */
    public static final int OVERFLOW_FLAG       = (1 << 6);
    /** Negative Flag (N) - Bit 7 */
    public static final int NEGATIVE_FLAG       = (1 << 7);
    
    // --- Memory Addresses ---
    /** Low byte address for the Reset Vector (0xFFFC). */
    public static final int RESET_VECTOR_LOW = 0xFFFC;
    /** High byte address for the Reset Vector (0xFFFD). */
    public static final int RESET_VECTOR_HIGH = 0xFFFD;
    /** Initial value for the Stack Pointer on reset (0xFD). */
    public static final int INITIAL_STACK_POINTER = 0xFD;

    // --- General Bitwise and Value Constants ---
    /** Mask for extracting the lowest 8 bits (to simulate unsigned byte). */
    public static final int BYTE_MASK = 0xFF;
    /** Number of bits in a byte. */
    public static final int BITS_PER_BYTE = 8;
    /** Common initial value for registers/flags (0). */
    public static final int INITIAL_VALUE = 0;
    /** Mask for 16-bit addresses (0xFFFF). */
    public static final int MAX_ADDRESS_VALUE = 0xFFFF;
    /** Maximum value for a signed 8-bit number (0x7F). */
    public static final int SIGNED_BYTE_MAX = 0x7F;
    /** Value used for 8-bit wrap-around calculations (0x100). */
    public static final int BYTE_WRAP = 0x100;
}
