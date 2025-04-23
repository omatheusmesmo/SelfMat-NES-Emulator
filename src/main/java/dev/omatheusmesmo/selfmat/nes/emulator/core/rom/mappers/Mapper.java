package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers;

/**
 * Abstract base class for all NES memory mappers.
 * Controls how CPU and PPU access cartridge's PRG and CHR memory.
 */
public abstract class Mapper {

    // --- Constants: Bank Sizes ---
    /** Standard size of a PRG ROM bank in bytes (16 KiB). */
    protected static final int PRG_BANK_SIZE_BYTES = 16 * 1024; // 16384
    /** Standard size of a CHR ROM/RAM bank in bytes (8 KiB). */
    protected static final int CHR_BANK_SIZE_BYTES = 8 * 1024;  // 8192

    // --- Constants: Standard NES Address Ranges ---
    /** Start address of the common CPU-mapped PRG RAM range. */
    protected static final int CPU_PRG_RAM_START = 0x6000;
    /** End address of the common CPU-mapped PRG RAM range. */
    protected static final int CPU_PRG_RAM_END = 0x7FFF;
    /** Start address of the standard CPU-mapped PRG ROM range. */
    protected static final int CPU_PRG_ROM_START = 0x8000;
    /** End address of the standard CPU-mapped PRG ROM range. */
    protected static final int CPU_PRG_ROM_END = 0xFFFF;
    /** Start address of the PPU-mapped CHR ROM/RAM range. */
    protected static final int PPU_CHR_START = 0x0000;
    /** End address of the PPU-mapped CHR ROM/RAM range (also serves as mask). */
    protected static final int PPU_CHR_END = 0x1FFF;


    // --- Fields ---
    /** The iNES mapper number. */
    protected final int mapperNumber;

    /** Number of 16KB PRG ROM banks. */
    protected final int prgBanks;

    /** Number of 8KB CHR ROM banks (0 indicates CHR RAM). */
    protected final int chrBanks;

    /** Raw PRG ROM data. Needs to be populated via loadRomData(). */
    protected byte[] prgRomData;

    /** CHR data (ROM or RAM). Needs to be populated via loadRomData() or allocated if RAM. */
    protected byte[] chrData;

    /** PRG RAM data. Allocation might be needed in subclasses or based on flags. */
    protected byte[] prgRamData;

    /** Static mirroring type from header (true=Vertical, false=Horizontal). Can be overridden. */
    protected boolean isVerticalMirroring;

    /**
     * Constructs a new Mapper instance.
     * Initializes metadata based on header info. Actual ROM data must be loaded separately.
     *
     * @param mapperNumber        The iNES mapper number.
     * @param prgRomSizeBytes     Total size of PRG ROM in bytes.
     * @param chrDataSizeBytes    Total size of CHR ROM in bytes (0 if CHR RAM is used).
     * @param isVerticalMirroring Initial mirroring type from the header.
     */
    public Mapper(int mapperNumber, int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        this.mapperNumber = mapperNumber;
        this.isVerticalMirroring = isVerticalMirroring;

        // Calculate banks using constants
        this.prgBanks = prgRomSizeBytes / PRG_BANK_SIZE_BYTES;
        this.chrBanks = chrDataSizeBytes == 0 ? 0 : chrDataSizeBytes / CHR_BANK_SIZE_BYTES;

        // Allocate CHR RAM if needed, using constant size
        if (this.chrBanks == 0 && chrDataSizeBytes == 0) {
            this.chrData = new byte[CHR_BANK_SIZE_BYTES];
            System.out.println("Mapper allocated " + (CHR_BANK_SIZE_BYTES / 1024) + "KB CHR RAM.");
        }
        // Note: PRG RAM allocation logic might depend on header flags (e.g., battery)
        // or mapper type, often handled by the Cartridge/Loader or in specific mapper subclasses.
        // Example: if (header.hasBattery()) { this.prgRamData = new byte[8192]; }
    }

    /**
     * Loads the actual PRG and CHR ROM data arrays into the mapper.
     * Should be called after construction and after data is read from the file.
     *
     * @param prgRomData The byte array containing the PRG ROM data.
     * @param chrRomData The byte array containing the CHR ROM data (used only if chrBanks > 0).
     */
    public void loadRomData(byte[] prgRomData, byte[] chrRomData) {
        this.prgRomData = prgRomData;
        // Only assign chrRomData if the cartridge uses CHR ROM.
        if (this.chrBanks > 0) {
            this.chrData = chrRomData;
        }
        // Potential place to initialize/load PRG RAM if applicable
    }

    // --- Abstract Core I/O Methods ---

    /**
     * Handles a CPU read request from the mapper's address range (typically $6000 - $FFFF).
     * Must be implemented by subclasses to perform mapping logic.
     * @param address The 16-bit CPU address.
     * @return The byte read from PRG ROM/RAM or mapper registers.
     */
    public abstract byte cpuRead(int address);

    /**
     * Handles a CPU write request to the mapper's address range (typically $6000 - $FFFF).
     * Must be implemented by subclasses to perform mapping logic.
     * @param address The 16-bit CPU address.
     * @param data The byte to write.
     */
    public abstract void cpuWrite(int address, byte data);

    /**
     * Handles a PPU read request from the CHR address range ($0000 - $1FFF).
     * Must be implemented by subclasses to perform mapping logic.
     * @param address The 14-bit PPU address (often passed as 16-bit).
     * @return The byte read from CHR ROM/RAM.
     */
    public abstract byte ppuRead(int address);

    /**
     * Handles a PPU write request to the CHR address range ($0000 - $1FFF).
     * Must be implemented by subclasses (mainly affects CHR RAM).
     * @param address The 14-bit PPU address (often passed as 16-bit).
     * @param data The byte to write.
     */
    public abstract void ppuWrite(int address, byte data);


    // --- Optional Methods for Advanced Mappers ---

    /**
     * Returns the current mirroring mode (true=Vertical, false=Horizontal).
     * Default returns static mirroring. Override for dynamic mirroring mappers.
     */
    public boolean getMirroringType() {
        return isVerticalMirroring;
    }

    /**
     * Signals a CPU clock cycle. Useful for cycle-counting mappers (e.g., MMC3).
     * Default implementation does nothing.
     */
    public void clock() {
        // Override in specific mappers if needed
    }

    /**
     * Signals the end of a PPU scanline. Useful for scanline-counting mappers (e.g., MMC3 IRQ).
     * @return true if the mapper generated an IRQ, false otherwise.
     */
    public boolean onScanline() {
        // Override in specific mappers if needed
        return false; // Default: no IRQ
    }

    /**
     * Resets the mapper's internal state to its default (power-on) state.
     * Subclasses should override to reset bank registers, counters, etc.
     */
    public void reset() {
        // Override in specific mappers if needed
    }

    // --- Basic Getters ---

    public int getMapperNumber() {
        return mapperNumber;
    }
}