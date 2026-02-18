package dev.omatheusmesmo.selfmat.nes.emulator.core.memory;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.ICartridge;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusTest {

    private Bus bus;
    private MockCartridge mockCartridge; // Usando a classe mock específica

    // Mock manual para ICartridge
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

    @Test
    @DisplayName("Should write and read byte from PPU registers correctly with mirroring")
    void shouldWriteAndReadByteFromPpuRegistersCorrectly() {
        int address = 0x2000;
        byte expectedValue = (byte) 0x12;
        bus.write(address, expectedValue);
        assertEquals(expectedValue, bus.read(address));

        // Test PPU mirroring (0x2000-0x3FFF mirrors 0x2000-0x2007)
        int mirroredAddress = 0x2008; // Should map to 0x2000
        bus.write(mirroredAddress, (byte) 0x34);
        assertEquals((byte) 0x34, bus.read(address)); // Read original 0x2000
        assertEquals((byte) 0x34, bus.read(mirroredAddress)); // Read mirrored 0x2008

        mirroredAddress = 0x3FF7; // Should map to 0x2007
        bus.write(mirroredAddress, (byte) 0x56);
        assertEquals((byte) 0x56, bus.read(0x2007));
        assertEquals((byte) 0x56, bus.read(mirroredAddress));
    }

    @Test
    @DisplayName("Should write and read byte from APU/IO registers correctly")
    void shouldWriteAndReadByteFromApuIoRegistersCorrectly() {
        int address = 0x4000;
        byte expectedValue = (byte) 0x78;
        bus.write(address, expectedValue);
        assertEquals(expectedValue, bus.read(address));

        // Test writing to a normal APU register
        int apuRegisterAddress = 0x4005;
        byte apuValue = (byte) 0x9A;
        bus.write(apuRegisterAddress, apuValue);
        assertEquals(apuValue, bus.read(apuRegisterAddress));
    }

    @Test
    @DisplayName("Should trigger OAM DMA action on write to APU_OAM_DMA_ADDRESS")
    void shouldTriggerOamDmaAction() {
        int oamDmaAddress = 0x4014; // APU_OAM_DMA_ADDRESS
        byte data = (byte) 0x02; // Page of CPU RAM to transfer

        // Mocking System.out.println to capture output
        var originalOut = System.out;
        var newOut = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(newOut));

        bus.write(oamDmaAddress, data);

        String expectedOutput = "OAM DMA triggered with data: 2\n";
        assertEquals(expectedOutput, newOut.toString());

        System.setOut(originalOut); // Restore original System.out
    }

    @Test
    @DisplayName("Should print message on write to Controller 1 and 2 addresses")
    void shouldPrintMessageOnControllerWrites() {
        int controller1Address = 0x4016;
        int controller2Address = 0x4017;
        byte data = (byte) 0x01; // Any data

        var originalOut = System.out;
        var newOut = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(newOut));

        bus.write(controller1Address, data);
        bus.write(controller2Address, data);

        String expectedOutput = "Controller 1 write with data: 1\n" +
                                "Controller 2 write with data: 1\n";
        assertEquals(expectedOutput, newOut.toString());

        System.setOut(originalOut); // Restore original System.out
    }

    @Test
    @DisplayName("Should return 0 for Controller 1 and 2 reads (stubbed)")
    void shouldReturnZeroForControllerReads() {
        assertEquals((byte) 0x00, bus.read(0x4016)); // APU_CONTROLLER_1_ADDRESS
        assertEquals((byte) 0x00, bus.read(0x4017)); // APU_CONTROLLER_2_ADDRESS
    }
}
