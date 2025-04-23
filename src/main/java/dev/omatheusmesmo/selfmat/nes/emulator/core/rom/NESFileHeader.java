package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import java.util.List;

public class NESFileHeader {
    private final List<Byte> header;

    public NESFileHeader(List<Byte> header) {
        this.header = header;
    }

    public boolean isValid() {
        return header.get(0) == (byte) 0x4E && header.get(1) == (byte) 0x45 &&
               header.get(2) == (byte) 0x53 && header.get(3) == (byte) 0x1A;
    }

    public int getPrgRomSize() {
        return header.get(4) * 16; // Size in KB
    }

    public int getChrRomSize() {
        return header.get(5) == 0 ? 8 : header.get(5) * 8; // Size in KB
    }

    public int getMapperNumber() {
        byte flag6 = header.get(6);
        byte flag7 = header.get(7);
        return ((flag7 >> 4) & 0x0F) | ((flag6 & 0xF0) << 4);
    }

    public boolean isVerticalMirroring() {
        byte flag6 = header.get(6);
        return ((flag6 >> 7) & 1) == 0; // 0 = horizontal, 1 = vertical
    }

    public boolean usesBattery() {
        byte flag6 = header.get(6);
        return ((flag6 >> 6) & 2) == 1;
    }

    public boolean hasTrainer() {
        byte flag6 = header.get(6);
        return ((flag6 >> 5) & 3) == 1;
    }

    public boolean isInes2() {
        return header.get(7) == (byte) 0x08;
    }
}
