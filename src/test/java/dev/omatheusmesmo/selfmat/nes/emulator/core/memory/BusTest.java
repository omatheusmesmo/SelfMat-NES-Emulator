package dev.omatheusmesmo.selfmat.nes.emulator.core.memory;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusTest {

    private Bus bus;
    private ICartridge mockCartridge;

    @BeforeEach
    void setUp() {
        // Criamos uma implementação manual para os testes.
        mockCartridge = new ICartridge() {
            private byte lastCpuReadValue = 0x00;
            private byte lastCpuWriteValue = 0x00;
            private int lastCpuWriteAddress = 0x0000;

            @Override
            public byte cpuRead(int address) {
                return lastCpuReadValue;
            }

            @Override
            public void cpuWrite(int address, byte data) {
                this.lastCpuWriteAddress = address;
                this.lastCpuWriteValue = data;
            }

            @Override
            public Mapper getMapper() { return null; } // Not needed for Bus tests

            @Override
            public void loadMapperData(byte[] prgRomData, byte[] chrRomData) { /* Not needed for Bus tests */ }

            public void setCpuReadValue(byte value) {
                this.lastCpuReadValue = value;
            }

            public byte getLastCpuWriteValue() {
                return lastCpuWriteValue;
            }

            public int getLastCpuWriteAddress() {
                return lastCpuWriteAddress;
            }
        };
        bus = new Bus(mockCartridge);
    }

    @Test
    @DisplayName("Should write and read byte from CPU RAM correctly")
    void shouldWriteAndReadByteFromCpuRamCorrectly() {
        int address = 0x0010;
        byte expectedValue = (byte) 0xAB;
        bus.write(address, expectedValue);
        assertEquals(expectedValue, bus.read(address));
    }

    @Test
    @DisplayName("Should handle CPU RAM mirroring correctly")
    void shouldHandleCpuRamMirroringCorrectly() {
        int originalAddress = 0x0050;
        int mirroredAddress1 = 0x0850; // original + 0x800
        int mirroredAddress2 = 0x1050; // original + 0x1000
        int mirroredAddress3 = 0x1850; // original + 0x1800
        byte expectedValue = (byte) 0xCD;

        // Write to original address
        bus.write(originalAddress, expectedValue);

        // Read from mirrored addresses
        assertEquals(expectedValue, bus.read(mirroredAddress1));
        assertEquals(expectedValue, bus.read(mirroredAddress2));
        assertEquals(expectedValue, bus.read(mirroredAddress3));

        // Write to a mirrored address and check original
        byte newValue = (byte) 0xEF;
        bus.write(mirroredAddress1, newValue);
        assertEquals(newValue, bus.read(originalAddress));
    }
}
