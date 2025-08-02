package dev.omatheusmesmo.selfmat.nes.emulator.core.ppu;

/**
 * Represents the different PPU nametable mirroring modes.
 */
public enum MirroringMode {
    /** Nametables are arranged for horizontal scrolling. */
    HORIZONTAL,

    /** Nametables are arranged for vertical scrolling. */
    VERTICAL,

    /** All four virtual nametables map to the same physical VRAM bank. */
    SINGLE_SCREEN_LOWER,

    /** All four virtual nametables map to the other physical VRAM bank. */
    SINGLE_SCREEN_UPPER,

    /** The cartridge provides extra VRAM for four distinct screens. Rare. */
    FOUR_SCREEN
}