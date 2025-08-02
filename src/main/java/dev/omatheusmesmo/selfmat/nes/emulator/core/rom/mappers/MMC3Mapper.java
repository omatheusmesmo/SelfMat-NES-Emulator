package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers;

import dev.omatheusmesmo.selfmat.nes.emulator.core.ppu.MirroringMode;

/**
 * Implements Mapper 4 (MMC3).
 * <p>
 * The MMC3 is one of the most advanced and common mappers, used in iconic games
 * like Super Mario Bros. 3 and Kirby's Adventure. Its key features include:
 * - Highly flexible PRG and CHR bank switching.
 * - A scanline-based IRQ counter for advanced graphical effects (split-screen).
 * - PRG RAM protection.
 */
public class MMC3Mapper extends Mapper {

    // --- Bank Switching Registers ---
    private final int[] chrBankRegisters = new int[6];
    private final int[] prgBankRegisters = new int[2];
    private int bankSelectRegister;

    // --- Bank Switching Configuration ---
    private boolean prgBankMode; // false: $8000 swappable, $C000 fixed; true: $C000 swappable, $8000 fixed
    private boolean chrInversionMode; // false: 2k+2k+1k+1k+1k+1k; true: 1k*4 + 2k*2

    // --- Calculated Memory Bank Offsets ---
    private final int[] prgBankOffsets = new int[4]; // For $8000, $A000, $C000, $E000
    private final int[] chrBankOffsets = new int[8]; // For 8x 1KB CHR banks

    // --- IRQ (Interrupt Request) State ---
    private int irqCounter;
    private int irqLatchValue;
    private boolean irqEnabled;
    private boolean irqPending;
    private boolean irqReloadFlag;

    // --- PRG RAM State ---
    private boolean prgRamEnabled;
    private boolean prgRamWriteProtect;

    public MMC3Mapper(int mapperNumber, int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        super(mapperNumber, prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
        this.prgRamData = new byte[8 * 1024]; // MMC3 typically has 8KB PRG RAM
        reset();
    }

    @Override
    public void reset() {
        bankSelectRegister = 0;
        prgBankMode = false;
        chrInversionMode = false;

        for (int i = 0; i < chrBankRegisters.length; i++) chrBankRegisters[i] = 0;
        for (int i = 0; i < prgBankRegisters.length; i++) prgBankRegisters[i] = 0;

        irqCounter = 0;
        irqLatchValue = 0;
        irqEnabled = false;
        irqPending = false;
        irqReloadFlag = false;

        prgRamEnabled = true;
        prgRamWriteProtect = false;

        // Initialize with a known state
        updateBankOffsets();
    }

    @Override
    public byte cpuRead(int address) {
        address &= 0xFFFF;

        if (address >= CPU_PRG_RAM_START && address <= CPU_PRG_RAM_END) {
            if (prgRamEnabled) {
                return prgRamData[address - CPU_PRG_RAM_START];
            }
            return 0; // Open bus
        }

        if (address >= CPU_PRG_ROM_START) {
            int bankIndex = (address - CPU_PRG_ROM_START) / 0x2000; // 8KB bank index
            int offset = address & 0x1FFF;
            return prgRomData[prgBankOffsets[bankIndex] + offset];
        }
        return 0;
    }

    @Override
    public void cpuWrite(int address, byte data) {
        address &= 0xFFFF;

        if (address >= CPU_PRG_RAM_START && address <= CPU_PRG_RAM_END) {
            if (prgRamEnabled && !prgRamWriteProtect) {
                prgRamData[address - CPU_PRG_RAM_START] = data;
            }
            return;
        }

        if (address >= CPU_PRG_ROM_START) {
            // Check if address is even or odd
            boolean isOdd = (address & 1) == 1;

            if (address <= 0x9FFF) { // Bank select and bank data registers
                if (!isOdd) { // $8000-$9FFE, even: Bank Select
                    bankSelectRegister = data & 0x07;
                    prgBankMode = (data & 0x40) != 0;
                    chrInversionMode = (data & 0x80) != 0;
                } else { // $8001-$9FFF, odd: Bank Data
                    updateBankData(data);
                }
            } else if (address <= 0xBFFF) { // Mirroring and PRG RAM protect
                if (!isOdd) { // $A000-$BFFE, even: Mirroring
                    setMirroringMode((data & 1) == 0 ? MirroringMode.VERTICAL : MirroringMode.HORIZONTAL);
                } else { // $A001-$BFFF, odd: PRG RAM Protect
                    prgRamEnabled = (data & 0x80) != 0;
                    prgRamWriteProtect = (data & 0x40) != 0;
                }
            } else if (address <= 0xDFFF) { // IRQ Control
                if (!isOdd) { // $C000-$DFFE, even: IRQ Latch
                    irqLatchValue = data;
                } else { // $C001-$DFFF, odd: IRQ Reload
                    irqCounter = 0;
                    irqReloadFlag = true;
                }
            } else { // $E000-$FFFF: IRQ Acknowledge/Enable
                if (!isOdd) { // $E000-$FFFE, even: IRQ Disable
                    irqEnabled = false;
                    irqPending = false; // Acknowledge any pending IRQ
                } else { // $E001-$FFFF, odd: IRQ Enable
                    irqEnabled = true;
                }
            }
        }
    }

    private void updateBankData(byte data) {
        switch (bankSelectRegister) {
            // CHR Banks
            case 0: case 1: case 2: case 3: case 4: case 5:
                chrBankRegisters[bankSelectRegister] = data;
                break;
            // PRG Banks
            case 6: case 7:
                prgBankRegisters[bankSelectRegister - 6] = data;
                break;
        }
        updateBankOffsets();
    }

    private void updateBankOffsets() {
        // --- PRG ROM Bank Mapping (4 x 8KB banks) ---
        int prgBankCount = prgRomData.length / 0x2000; // Number of 8KB banks
        int fixedBank = (prgBankCount - 1) * 0x2000; // Last 8KB bank is always fixed at $E000

        if (!prgBankMode) {
            // $8000 is swappable, $C000 is fixed to the second-to-last bank
            prgBankOffsets[0] = (prgBankRegisters[0] & 0x3F) * 0x2000;
            prgBankOffsets[1] = (prgBankRegisters[1] & 0x3F) * 0x2000;
            prgBankOffsets[2] = (prgBankCount - 2) * 0x2000;
            prgBankOffsets[3] = fixedBank;
        } else {
            // $C000 is swappable, $8000 is fixed to the second-to-last bank
            prgBankOffsets[0] = (prgBankCount - 2) * 0x2000;
            prgBankOffsets[1] = (prgBankRegisters[1] & 0x3F) * 0x2000;
            prgBankOffsets[2] = (prgBankRegisters[0] & 0x3F) * 0x2000;
            prgBankOffsets[3] = fixedBank;
        }

        // --- CHR ROM/RAM Bank Mapping (8 x 1KB banks) ---
        if (!chrInversionMode) {
            // R0, R1 are 2KB banks; R2-R5 are 1KB banks
            chrBankOffsets[0] = (chrBankRegisters[0] & 0xFE) * 0x0400;
            chrBankOffsets[1] = chrBankOffsets[0] + 0x0400;
            chrBankOffsets[2] = (chrBankRegisters[1] & 0xFE) * 0x0400;
            chrBankOffsets[3] = chrBankOffsets[2] + 0x0400;
            chrBankOffsets[4] = chrBankRegisters[2] * 0x0400;
            chrBankOffsets[5] = chrBankRegisters[3] * 0x0400;
            chrBankOffsets[6] = chrBankRegisters[4] * 0x0400;
            chrBankOffsets[7] = chrBankRegisters[5] * 0x0400;
        } else {
            // R0, R1 are 1KB banks; R2-R5 are 1KB banks; R6, R7 are 2KB banks
            chrBankOffsets[0] = chrBankRegisters[2] * 0x0400;
            chrBankOffsets[1] = chrBankRegisters[3] * 0x0400;
            chrBankOffsets[2] = chrBankRegisters[4] * 0x0400;
            chrBankOffsets[3] = chrBankRegisters[5] * 0x0400;
            chrBankOffsets[4] = (chrBankRegisters[0] & 0xFE) * 0x0400;
            chrBankOffsets[5] = chrBankOffsets[4] + 0x0400;
            chrBankOffsets[6] = (chrBankRegisters[1] & 0xFE) * 0x0400;
            chrBankOffsets[7] = chrBankOffsets[6] + 0x0400;
        }
    }

    @Override
    public byte ppuRead(int address) {
        address &= PPU_CHR_END;
        if (chrData.length == 0) return 0;

        int bankIndex = address / 0x0400; // 1KB bank index
        int offset = address & 0x03FF;
        return chrData[(chrBankOffsets[bankIndex] + offset) % chrData.length];
    }

    @Override
    public void ppuWrite(int address, byte data) {
        if (!isChrRam) return;
        address &= PPU_CHR_END;
        if (chrData.length == 0) return;

        int bankIndex = address / 0x0400; // 1KB bank index
        int offset = address & 0x03FF;
        chrData[(chrBankOffsets[bankIndex] + offset) % chrData.length] = data;
    }

    /**
     * This method is the core of the MMC3's IRQ system.
     * It should be called by the PPU at the end of every visible scanline.
     * @return true if an IRQ should be triggered, false otherwise.
     */
    @Override
    public boolean onScanline() {
        if (irqReloadFlag) {
            irqCounter = irqLatchValue;
            irqReloadFlag = false;
        } else if (irqCounter > 0) {
            irqCounter--;
        }

        if (irqCounter == 0 && irqEnabled) {
            irqPending = true;
        }
        return irqPending;
    }
}