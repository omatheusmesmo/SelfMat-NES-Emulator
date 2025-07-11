package dev.omatheusmesmo.selfmat.nes.emulator.core.rom;

import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.Mapper;
import dev.omatheusmesmo.selfmat.nes.emulator.core.rom.mappers.factory.MapperFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class MapperManager {

    private static final Map<Integer, MapperFactory> factories = new HashMap<>();

    static {
        System.out.println("MapperManager: Initializing and loading factories via ServiceLoader...");

        ServiceLoader<MapperFactory> loader = ServiceLoader.load(MapperFactory.class);

        for (MapperFactory factory : loader) {
            int mapperNumber = factory.getSupportedMapperNumber();
            if (factories.containsKey(mapperNumber)) {
                System.err.printf("Warning: Duplicate factory registration for mapper #%d. Using %s, ignoring previous.\n",
                        mapperNumber, factory.getClass().getName());
            }
            System.out.printf("MapperManager: Registering factory for mapper #%d (%s)\n",
                    mapperNumber, factory.getClass().getSimpleName());
            factories.put(mapperNumber, factory);
        }
        System.out.println("MapperManager: Factory loading complete. Registered mappers: " + factories.keySet());
    }

    /**
     * Creates and returns the appropriate Mapper instance using a registered factory.
     *
     * @param mapperNumber        The iNES mapper number from the ROM header.
     * @param prgRomSizeBytes     Total size of PRG ROM in bytes.
     * @param chrDataSizeBytes    Total size of CHR ROM/RAM in bytes (0 for RAM).
     * @param isVerticalMirroring Initial mirroring type from the header.
     * @return An initialized Mapper instance corresponding to the mapperNumber.
     * @throws UnsupportedOperationException if no factory is registered for the mapper number.
     */
    public static Mapper createMapper(int mapperNumber, int prgRomSizeBytes, int chrDataSizeBytes, boolean isVerticalMirroring) {
        MapperFactory factory = factories.get(mapperNumber);

        if (factory != null) {
            System.out.println("MapperManager: Found factory for mapper #" + mapperNumber + ". Creating instance...");
            return factory.create(prgRomSizeBytes, chrDataSizeBytes, isVerticalMirroring);
        } else {
            System.err.println("MapperManager Error: No factory registered for mapper number: " + mapperNumber);
            throw new UnsupportedOperationException("Mapper " + mapperNumber + " is not supported (no factory registered).");
        }
    }
}