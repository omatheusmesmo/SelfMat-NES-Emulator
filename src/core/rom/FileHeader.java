package core.rom;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHeader {
    String filePath = "/home/omatheusmesmo/Downloads/Super Mario Bros. (World).nes"; // Caminho do arquivo

    List<Byte> header = readHeader(filePath);

    public FileHeader() {
    }

    public void loadRomHeader(String filePath) {
            boolean nesFileValid = validateHeader(header);
            int prgRomSize = getPrgRomSize(header);
            int chrRomSize = getChrRomSize(header);
            boolean inesVersion = checkInes2(header);
            int mapperNumber = getMapperNumber(header);
            boolean isVerticalMirroring = getMirroring(header);
            boolean usesBattery = usesBattery(header);
            boolean hasTrainer = isTrainer(header);

            System.out.println("\nResumo do Header:");
            System.out.println("NES File Valid: " + (nesFileValid ? "Yes" : "No"));
            System.out.println("PRG ROM Size: " + prgRomSize + " KB");
            System.out.println("CHR ROM Size: " + chrRomSize + " KB");
            System.out.println("Mapper Number: " + mapperNumber);
            System.out.println("Mirroring: " + (isVerticalMirroring ? "Vertical" : "Horizontal"));
            System.out.println("Uses Battery: " + (usesBattery ? "Yes" : "No"));
            System.out.println("Has Trainer: " + (hasTrainer ? "Yes" : "No"));
            System.out.println("iNES Version: " + (inesVersion ? "2.0" : "1.0"));
    }

    private boolean isTrainer(List<Byte> header) {
        byte flag6 = header.get(6);
        return ((flag6 >> 5) & 3) == 1;
    }

    private boolean usesBattery(List<Byte> header) {
        byte flag6 = header.get(6);
        return ((flag6 >> 6) & 2) == 1;
    }

    private boolean getMirroring(List<Byte> header) {
        byte flag6 = header.get(6);
        return ((flag6 >> 7) & 1) == 0; // 0 = horizontal, 1 = vertical
    }

    private int getMapperNumber(List<Byte> header) {
        byte flag6 = header.get(6);
        byte flag7 = header.get(7);
        return ((flag7 >> 4) & 0x0F) | ((flag6 & 0xF0) << 4);
    }

    private int getPrgRomSize(List<Byte> header) {
        return header.get(4) * 16;
    }

    private int getChrRomSize(List<Byte> header) {
        if (header.get(5) == 0) {
            return 8;
        }
        return header.get(5) * 8;
    }

    private boolean checkInes2(List<Byte> header) {
        return header.get(7) == (byte) 0x08;
    }

    private List<Byte> readHeader(String filePath) {
        List<Byte> header = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            for (int i = 0; i < 16; i++) {
                int byteValue = fis.read();
                if (byteValue == -1) {
                    break;
                }
                header.add((byte) byteValue);  // Armazenando como byte
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
        return header;
    }

    private boolean validateHeader(List<Byte> header) {
        return header.get(0) == (byte) 0x4E && header.get(1) == (byte) 0x45 &&
                header.get(2) == (byte) 0x53 && header.get(3) == (byte) 0x1A;
    }
}
