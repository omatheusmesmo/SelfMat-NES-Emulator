package dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers;

import dev.omatheusmesmo.selfmat.nes.emulator.core.ppu.MirroringMode;

/**
 * Implements Mapper 1 (MMC1).
 * <p>
 * MMC1 is a versatile mapper that introduces PRG and CHR bank switching,
 * as well as dynamic mirroring control. Its primary feature is the use of a serial
 * shift register. Configurations are not written directly; instead, 5 sequential
 * writes to the $8000-$FFFF range load a 5-bit value, which is then transferred
 * to one of four internal registers based on the write address.
 */
public class MMC1Mapper extends Mapper {

    // --- Constants ---
    private static final int PRG_16K_BANK_SIZE_BYTES = 16 * 1024;
    private static final int CHR_4K_BANK_SIZE_BYTES = 4 * 1024;

    // --- Internal State ---
    private int shiftRegister;
    private int writeCount;

    // MMC1 Internal Registers (5-bit)
    private int controlRegister;
    private int chrBank0Register;
    private int chrBank1Register;
    private int prgBankRegister;

    // Calculated memory bank offsets
    private int prgBankOffset0; // For the slot at $8000
    private int prgBankOffset1; // For the slot at $C000
    private int chrBankOffset0; // For the slot at $0000
    private int chrBankOffset1; // For the slot at $1000

    private boolean prgRamEnabled;

    public MMC1Mapper(int mapperNumber, int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        super(mapperNumber, prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
        // MMC1 often has 8KB of PRG RAM, which can be enabled/disabled.
        this.prgRamData = new byte[8 * 1024];
        reset();
    }

    @Override
    public void reset() {
        // On power-up/reset, control register is set to $0C.
        // This sets PRG mode 3 (fix last bank, switch first) and 4KB CHR mode.
        controlRegister = 0x0C;
        chrBank0Register = 0;
        chrBank1Register = 0;
        prgBankRegister = 0;
        prgRamEnabled = true; // PRG RAM is typically enabled by default
        resetShiftRegister();
        updateBankOffsets();
    }

    private void resetShiftRegister() {
        shiftRegister = 0;
        writeCount = 0;
    }

    @Override
    public byte cpuRead(int address) {
        address &= 0xFFFF;

        if (address >= CPU_PRG_RAM_START && address <= CPU_PRG_RAM_END) {
            if (prgRamEnabled) {
                return prgRamData[address - CPU_PRG_RAM_START];
            }
            return 0; // Open bus behavior if PRG RAM is disabled
        }

        if (address >= CPU_PRG_ROM_START) {
            if (address < 0xC000) { // Slot $8000 - $BFFF
                int mappedAddress = prgBankOffset0 + (address - 0x8000);
                return prgRomData[mappedAddress % prgRomData.length];
            } else { // Slot $C000 - $FFFF
                int mappedAddress = prgBankOffset1 + (address - 0xC000);
                return prgRomData[mappedAddress % prgRomData.length];
            }
        }
        return 0;
    }

    @Override
    public void cpuWrite(int address, byte data) {
        address &= 0xFFFF;

        if (address >= CPU_PRG_RAM_START && address <= CPU_PRG_RAM_END) {
            if (prgRamEnabled) {
                prgRamData[address - CPU_PRG_RAM_START] = data;
            }
            return;
        }

        if (address >= CPU_PRG_ROM_START) {
            if ((data & 0x80) != 0) {
                resetShiftRegister();
                controlRegister |= 0x0C;
                updateBankOffsets();
                return;
            }

            shiftRegister >>= 1;
            shiftRegister |= (data & 0x01) << 4;
            writeCount++;

            if (writeCount == 5) {
                int targetRegister = (address >> 13) & 0b11;

                switch (targetRegister) {
                    case 0: controlRegister = shiftRegister; break;
                    case 1: chrBank0Register = shiftRegister; break;
                    case 2: chrBank1Register = shiftRegister; break;
                    case 3:
                        prgBankRegister = shiftRegister;
                        prgRamEnabled = (prgBankRegister & 0x10) == 0;
                        break;
                }
                updateBankOffsets();
                resetShiftRegister();
            }
        }
    }

    @Override
    public byte ppuRead(int address) {
        address &= PPU_CHR_END;
        if (chrData.length == 0) return 0;

        if (address < 0x1000) { // Bank 0
            int mappedAddress = chrBankOffset0 + address;
            return chrData[mappedAddress % chrData.length];
        } else { // Bank 1
            int mappedAddress = chrBankOffset1 + (address - 0x1000);
            return chrData[mappedAddress % chrData.length];
        }
    }

    @Override
    public void ppuWrite(int address, byte data) {
        if (!isChrRam) return;

        address &= PPU_CHR_END;
        if (chrData.length == 0) return;

        if (address < 0x1000) { // Bank 0
            int mappedAddress = chrBankOffset0 + address;
            chrData[mappedAddress % chrData.length] = data;
        } else { // Bank 1
            int mappedAddress = chrBankOffset1 + (address - 0x1000);
            chrData[mappedAddress % chrData.length] = data;
        }
    }

    private void updateBankOffsets() {
        // --- Mirroring Control (Control Register bits 0-1) ---
        switch (controlRegister & 0b11) {
            case 0: setMirroringMode(MirroringMode.SINGLE_SCREEN_LOWER); break;
            case 1: setMirroringMode(MirroringMode.SINGLE_SCREEN_UPPER); break;
            case 2: setMirroringMode(MirroringMode.VERTICAL); break;
            case 3: setMirroringMode(MirroringMode.HORIZONTAL); break;
        }

        // --- PRG Bank Mode (Control Register bits 2-3) ---
        int prgMode = (controlRegister >> 2) & 0b11;
        int lastBankOffset = prgRomData.length - PRG_16K_BANK_SIZE_BYTES;

        switch (prgMode) {
            case 0:
            case 1: // 32KB switchable mode
                prgBankOffset0 = ((prgBankRegister & 0x0E) * PRG_16K_BANK_SIZE_BYTES);
                prgBankOffset1 = prgBankOffset0 + PRG_16K_BANK_SIZE_BYTES;
                break;
            case 2: // Fix first bank ($8000), switch second ($C000)
                prgBankOffset0 = 0;
                prgBankOffset1 = ((prgBankRegister & 0x0F) * PRG_16K_BANK_SIZE_BYTES);
                break;
            case 3: // Fix last bank ($C000), switch first ($8000)
                prgBankOffset0 = ((prgBankRegister & 0x0F) * PRG_16K_BANK_SIZE_BYTES);
                prgBankOffset1 = lastBankOffset;
                break;
        }

        // --- CHR Bank Mode (Control Register bit 4) ---
        int chrMode = (controlRegister >> 4) & 0b1;
        if (chrMode == 0) { // 8KB switchable mode
            chrBankOffset0 = ((chrBank0Register & 0x1E) * CHR_4K_BANK_SIZE_BYTES);
            chrBankOffset1 = chrBankOffset0 + CHR_4K_BANK_SIZE_BYTES;
        } else { // Two 4KB switchable banks
            chrBankOffset0 = (chrBank0Register * CHR_4K_BANK_SIZE_BYTES);
            chrBankOffset1 = (chrBank1Register * CHR_4K_BANK_SIZE_BYTES);
        }
    }
}