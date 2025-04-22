package main.java.dev.omatheusmesmo;


import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.FileHeader;

public class Main {
    public static void main(String[] args) {
        String filePath = "/home/omatheusmesmo/Downloads/Super Mario Bros. (World).nes"; // Caminho do arquivo


        FileHeader fileHeader = new FileHeader();

        fileHeader.loadRomHeader(filePath);


    }
}
