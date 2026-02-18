package dev.omatheusmesmo.selfmat.nes.emulator.core.memory;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.NESFileHeader;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusTest {

    private Bus bus;
    private MockCartridge mockCartridge;

    // Manual mock for ICartridge to avoid Mockito issues with records
    static class MockCartridge implements ICartridge {
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

        // Implementations for ICartridge methods not directly used by Bus tests
        @Override
        public NESFileHeader NESFileHeader() { return null; }
        @Override
        public byte[] prgRomData() { return null; }
        @Override
        public byte[] chrRomData() { return null; }
        @Override
        public byte[] trainerData() { return null; }

        public void setCpuReadValue(byte value) {
            this.lastCpuReadValue = value;
        }

        public byte getLastCpuWriteValue() {
            return lastCpuWriteValue;
        }

        public int getLastCpuWriteAddress() {
            return lastCpuWriteAddress;
        }
    }


    @BeforeEach
    void setUp() {
        mockCartridge = new MockCartridge();
        bus = new Bus(mockCartridge);
    }

    // --- RAM Tests ---

    @Test
    @DisplayName("Should write and read byte from CPU RAM correctly")
    void shouldWriteAndReadByteFromCpuRamCorrectly() {
        final int address = 0x0010;
        final byte expectedValue = (byte) 0xAB;
        bus.write(address, expectedValue);
        assertEquals(expectedValue, bus.read(address));
    }

    @Test
    @DisplayName("Should handle CPU RAM mirroring correctly")
    void shouldHandleCpuRamMirroringCorrectly() {
        final int originalAddress = 0x0050;
        final int mirroredAddress1 = 0x0850; // original + 0x800
        final int mirroredAddress2 = 0x1050; // original + 0x1000
        final int mirroredAddress3 = 0x1850; // original + 0x1800
        final byte expectedValue = (byte) 0xCD;

        // Write to original address
        bus.write(originalAddress, expectedValue);

        // Read from mirrored addresses
        assertEquals(expectedValue, bus.read(mirroredAddress1));
        assertEquals(expectedValue, bus.read(mirroredAddress2));
        assertEquals(expectedValue, bus.read(mirroredAddress3));

        // Write to a mirrored address and check original
        final byte newValue = (byte) 0xEF;
        bus.write(mirroredAddress1, newValue);
        assertEquals(newValue, bus.read(originalAddress));
    }

    // --- PPU Registers Tests ---

    @Test
    @DisplayName("Should write and read byte from PPU registers correctly with mirroring")
    void shouldWriteAndReadByteFromPpuRegistersCorrectly() {
        final int ppuRegisterAddress = 0x2000;
        final byte expectedValue = (byte) 0x12;
        bus.write(ppuRegisterAddress, expectedValue);
        assertEquals(expectedValue, bus.read(ppuRegisterAddress));

        // Test PPU mirroring (0x2000-0x3FFF mirrors 0x2000-0x2007)
        final int mirroredPpuAddress1 = 0x2008; // Should map to 0x2000
        bus.write(mirroredPpuAddress1, (byte) 0x34);
        assertEquals((byte) 0x34, bus.read(ppuRegisterAddress)); // Read original 0x2000
        assertEquals((byte) 0x34, bus.read(mirroredPpuAddress1)); // Read mirrored 0x2008

        final int mirroredPpuAddress2 = 0x3FF7; // Should map to 0x2007
        final int ppuRegister7Address = 0x2007;
        bus.write(mirroredPpuAddress2, (byte) 0x56);
        assertEquals((byte) 0x56, bus.read(ppuRegister7Address));
        assertEquals((byte) 0x56, bus.read(mirroredPpuAddress2));
    }

    // --- APU/IO Registers Tests ---

    @Test
    @DisplayName("Should write and read byte from APU/IO registers correctly")
    void shouldWriteAndReadByteFromApuIoRegistersCorrectly() {
        final int apuIoRegisterAddress = 0x4000;
        final byte expectedValue = (byte) 0x78;
        bus.write(apuIoRegisterAddress, expectedValue);
        assertEquals(expectedValue, bus.read(apuIoRegisterAddress));

        // Test writing to another normal APU register
        final int anotherApuRegisterAddress = 0x4005;
        final byte anotherApuValue = (byte) 0x9A;
        bus.write(anotherApuRegisterAddress, anotherApuValue);
        assertEquals(anotherApuValue, bus.read(anotherApuRegisterAddress));
    }

    @Test
    @DisplayName("Should trigger OAM DMA action on write to APU_OAM_DMA_ADDRESS")
    void shouldTriggerOamDmaAction() {
        final int oamDmaAddress = 0x4014;
        final byte dataPageValue = (byte) 0x02; // Page of CPU RAM to transfer

        // Mocking System.out.println to capture output
        var originalOut = System.out;
        var newOut = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(newOut));

        bus.write(oamDmaAddress, dataPageValue);

        String expectedOutput = "OAM DMA triggered with data: 2\n";
        assertEquals(expectedOutput, newOut.toString());

        System.setOut(originalOut); // Restore original System.out
    }

    @Test
    @DisplayName("Should print message on write to Controller 1 and 2 addresses")
    void shouldPrintMessageOnControllerWrites() {
        final int controller1Address = 0x4016;
        final int controller2Address = 0x4017;
        final byte arbitraryData = (byte) 0x01; // Any data

        var originalOut = System.out;
        var newOut = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(newOut));

        bus.write(controller1Address, arbitraryData);
        bus.write(controller2Address, arbitraryData);

        String expectedOutput = "Controller 1 write with data: 1\n" +
                                "Controller 2 write with data: 1\n";
        assertEquals(expectedOutput, newOut.toString());

        System.setOut(originalOut); // Restore original System.out
    }

    @Test
    @DisplayName("Should return 0 for Controller 1 and 2 reads (stubbed)")
    void shouldReturnZeroForControllerReads() {
        final int controller1Address = 0x4016;
        final int controller2Address = 0x4017;
        assertEquals((byte) 0x00, bus.read(controller1Address));
        assertEquals((byte) 0x00, bus.read(controller2Address));
    }

    // --- Cartridge Delegation Tests ---

    @Test
    @DisplayName("Should delegate CPU read to Cartridge for cartridge space")
    void shouldDelegateCpuReadToCartridge() {
        final int cartridgeReadAddress = 0x8000;
        final byte expectedReadValue = (byte) 0xCC;
        mockCartridge.setCpuReadValue(expectedReadValue); // Set mock to return this value

        assertEquals(expectedReadValue, bus.read(cartridgeReadAddress));
    }

    @Test
    @DisplayName("Should delegate CPU write to Cartridge for cartridge space")
    void shouldDelegateCpuWriteToCartridge() {
        final int cartridgeWriteAddress = 0xC000;
        final byte dataToWrite = (byte) 0xDD;
        bus.write(cartridgeWriteAddress, dataToWrite);

        assertEquals(dataToWrite, mockCartridge.getLastCpuWriteValue());
        assertEquals(cartridgeWriteAddress, mockCartridge.getLastCpuWriteAddress());
    }
}
