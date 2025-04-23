package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers;

/**
 * Implements Mapper 0 (NROM).
 * Supports 16KB or 32KB PRG ROM and 8KB CHR ROM or RAM.
 * Handles 16KB PRG ROM mirroring. No bank switching.
 */
public class NRomMapper extends Mapper {

    // --- NROM Specific Constants ---
    /** Mask applied to CPU addresses when PRG ROM is 16KB to handle mirroring ($8000-$BFFF mirrors $C000-$FFFF). */
    private static final int PRG_ROM_16K_MASK = 0x3FFF; // 16KB size - 1
    /** Offset subtracted from CPU addresses when PRG ROM is 32KB for direct mapping. */
    private static final int PRG_ROM_32K_OFFSET = 0x8000;

    /**
     * Constructs an NRomMapper instance.
     *
     * @param mapperNumber        Should be 0 for NROM.
     * @param prgRomSizeBytes     Size of PRG ROM (16384 or 32768).
     * @param chrDataSizeBytes    Size of CHR data (usually 8192, or 0 for CHR RAM).
     * @param isVerticalMirroring Initial mirroring type.
     */
    public NRomMapper(int mapperNumber, int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        super(mapperNumber, prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
        validateNromConfiguration(mapperNumber, prgBanks); // Example of using a private helper even in constructor
    }

    // --- CPU Access ---

    /**
     * Handles a CPU read request, dispatching to PRG RAM or PRG ROM handlers.
     *
     * @param address The 16-bit CPU address.
     * @return The byte read, or 0 if unmapped/unavailable.
     */
    @Override
    public byte cpuRead(int address) {
        address &= 0xFFFF; // Ensure 16-bit address

        if (isAddressInPrgRamRange(address)) {
            return handleCpuPrgRamRead(address);
        } else if (isAddressInPrgRomRange(address)) {
            return handleCpuPrgRomRead(address);
        }

        // Address is outside the ranges handled by this mapper
        return 0;
    }

    /**
     * Handles a CPU write request, dispatching to the PRG RAM handler if applicable.
     * NROM ignores writes to the PRG ROM area.
     *
     * @param address The 16-bit CPU address.
     * @param data    The byte to write.
     */
    @Override
    public void cpuWrite(int address, byte data) {
        address &= 0xFFFF; // Ensure 16-bit address

        if (isAddressInPrgRamRange(address)) {
            handleCpuPrgRamWrite(address, data);
        }
        // Writes to other ranges (like PRG ROM) are ignored by NROM.
    }

    // --- PPU Access ---

    /**
     * Handles a PPU read request from the CHR address range ($0000-$1FFF).
     *
     * @param address The 14-bit PPU address (masked to $0000-$1FFF).
     * @return The byte read, or 0 if unavailable/out of bounds.
     */
    @Override
    public byte ppuRead(int address) {
        address &= PPU_CHR_END; // Mask to PPU CHR range using constant from Mapper
        return handlePpuChrRead(address);
    }

    /**
     * Handles a PPU write request to the CHR address range ($0000-$1FFF).
     * Writes only affect CHR RAM.
     *
     * @param address The 14-bit PPU address (masked to $0000-$1FFF).
     * @param data    The byte to write.
     */
    @Override
    public void ppuWrite(int address, byte data) {
        address &= PPU_CHR_END; // Mask to PPU CHR range using constant from Mapper
        handlePpuChrWrite(address, data);
    }

    // --- Private Helper Methods ---

    /**
     * Checks if the given CPU address falls within the PRG RAM range.
     * @param address CPU address.
     * @return true if in PRG RAM range, false otherwise.
     */
    private boolean isAddressInPrgRamRange(int address) {
        return address >= CPU_PRG_RAM_START && address <= CPU_PRG_RAM_END;
    }

    /**
     * Checks if the given CPU address falls within the PRG ROM range.
     * @param address CPU address.
     * @return true if in PRG ROM range, false otherwise.
     */
    private boolean isAddressInPrgRomRange(int address) {
        return address >= CPU_PRG_ROM_START && address <= CPU_PRG_ROM_END;
    }

    /**
     * Handles reading from PRG RAM if available and within bounds.
     * @param address CPU address within PRG RAM range.
     * @return The byte read from PRG RAM, or 0 if unavailable/out of bounds.
     */
    private byte handleCpuPrgRamRead(int address) {
        if (prgRamData != null) {
            int index = address - CPU_PRG_RAM_START;
            // No need for index >= 0 check if address >= CPU_PRG_RAM_START is guaranteed
            if (index < prgRamData.length) {
                return prgRamData[index];
            }
        }
        return 0; // No PRG RAM or out of bounds
    }

    /**
     * Handles writing to PRG RAM if available and within bounds.
     * @param address CPU address within PRG RAM range.
     * @param data    The byte to write.
     */
    private void handleCpuPrgRamWrite(int address, byte data) {
        if (prgRamData != null) { // Check if PRG RAM exists
            int index = address - CPU_PRG_RAM_START;
            // No need for index >= 0 check if address >= CPU_PRG_RAM_START is guaranteed
            if (index < prgRamData.length) { // Check upper bound
                prgRamData[index] = data; // Perform to write
            }
            // else: Write is within range but outside allocated RAM size, ignore.
        }
        // else: Write to PRG RAM area, but no RAM exists, ignore.
    }

    /**
     * Handles reading from PRG ROM, applying NROM mapping logic.
     * @param address CPU address within PRG ROM range.
     * @return The byte read from PRG ROM, or 0 if unavailable/out of bounds.
     */
    private byte handleCpuPrgRomRead(int address) {
        if (prgRomData == null || prgRomData.length == 0) {
            // System.err.println("NRomMapper cpuRead Error: No PRG ROM data loaded.");
            return 0; // No ROM data
        }

        int mappedAddress = mapPrgRomAddress(address);

        // Final bounds check
        if (mappedAddress >= 0 && mappedAddress < prgRomData.length) {
            return prgRomData[mappedAddress];
        } else {
            // Should not happen with correct logic/data, indicates an issue
            System.err.printf("NRomMapper cpuRead Error: Address 0x%04X mapped to %d, out of bounds for PRG ROM size %d\n",
                    address, mappedAddress, prgRomData.length);
            return 0;
        }
    }

    /**
     * Maps a CPU address within the PRG ROM range ($8000-$FFFF) to an index
     * within the prgRomData array based on NROM logic (16KB mirroring or 32KB direct).
     * @param address CPU address ($8000-$FFFF).
     * @return The calculated index in prgRomData.
     */
    private int mapPrgRomAddress(int address) {
        if (prgBanks == 1) { // 16KB ROM: Apply mirroring mask
            return address & PRG_ROM_16K_MASK; // Use NROM specific constant
        } else { // 32KB ROM: Apply direct mapping offset
            return address - PRG_ROM_32K_OFFSET; // Use NROM specific constant
        }
    }

    /**
     * Handles reading directly from CHR data (ROM or RAM).
     * @param maskedAddress PPU address already masked to $0000-$1FFF.
     * @return The byte read, or 0 if unavailable/out of bounds.
     */
    private byte handlePpuChrRead(int maskedAddress) {
        // No need for maskedAddress >= 0 check as it's always positive after masking
        if (chrData != null && maskedAddress < chrData.length) {
            return chrData[maskedAddress];
        } else {
            // Error logging can be added here if desired
            return 0; // No CHR data or out of bounds
        }
    }

    /**
     * Handles writing to CHR RAM if applicable.
     * @param maskedAddress PPU address already masked to $0000-$1FFF.
     * @param data          The byte to write.
     */
    private void handlePpuChrWrite(int maskedAddress, byte data) {
        if (chrBanks == 0) { // Only write if using CHR RAM
            // No need for maskedAddress >= 0 check
            if (chrData != null && maskedAddress < chrData.length) {
                chrData[maskedAddress] = data;
            }
            // else: CHR RAM exists but address out of bounds (shouldn't happen with mask), ignore.
        }
        // else: Cartridge uses CHR ROM, ignore to write.
    }

    /**
     * Optional: Validates NROM specific configuration during construction.
     * @param mapperNumber The provided mapper number.
     * @param prgBanks The calculated number of PRG banks.
     */
    private void validateNromConfiguration(int mapperNumber, int prgBanks) {
        if (mapperNumber != 0) {
            System.err.println("Warning: NRomMapper created with mapper number " + mapperNumber);
        }
        if (prgBanks < 1 || prgBanks > 2) {
            System.err.println("Warning: NRomMapper created with unexpected prgBanks count: " + prgBanks);
        }
    }

    // NROM typically doesn't need to override clock(), onScanline(), or reset().
    // The default implementations in the Mapper base class are sufficient.
}