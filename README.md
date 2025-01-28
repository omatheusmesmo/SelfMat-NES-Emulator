# SelfMat-NES-Emulator

![Java](https://img.shields.io/badge/Java-21%2B-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 🎮 Project Overview

This project is a **Nintendo Entertainment System (NES) emulator** developed in Java. The goal is to replicate the functionality of the NES hardware in software, allowing classic games to run on modern platforms. The emulator implements key components of the NES, including:

- **CPU (6502)**: Simulation of the NES's main processor.
- **PPU (Picture Processing Unit)**: Rendering of graphics and sprites.
- **APU (Audio Processing Unit)**: Audio generation.
- **Mappers**: Address translation to support different game cartridges.

In addition to being a tribute to classic video games, this project demonstrates how fundamental Computer Science concepts can be applied to build complex and functional systems.

---

## 🎓 Emulation and Computer Science

Developing an NES emulator in Java is a challenging task that requires mastering fundamental Computer Science concepts. This project highlights the importance of understanding and applying key areas such as:

### 1. **Computer Architecture**
   - Detailed understanding of how a CPU operates, including instruction cycles, registers, and addressing modes.
   - Simulation of hierarchical memory systems (RAM, ROM, VRAM, etc.), essential for replicating the NES's behavior.

### 2. **Low-Level Programming**
   - Direct manipulation of bits and bytes to implement CPU operations and mappers.
   - Interpretation of `.nes` files in binary and hexadecimal formats, closely mirroring the original hardware's logic.

### 3. **Concurrency and Synchronization**
   - Coordination between independent components, such as the CPU, PPU, and APU, which run in parallel and must be synchronized for games to function correctly.

### 4. **Hardware Emulation**
   - Detailed recreation of components like the PPU, responsible for graphics, and the APU, responsible for audio.
   - Implementation of mappers, necessary to handle different memory layouts and support variations in game cartridges.

### 5. **Software Development**
   - Application of best practices, such as modularization, testing, and documentation, to manage highly complex systems.
   - Use of optimized algorithms and data structures to ensure the emulator performs efficiently.

### 6. **Mastering Java**
   - Advanced use of Java to create robust systems, including binary file manipulation, object-oriented programming, and real-time component integration.

---

## 🛠️ Technologies Used

- **Language**: Java 21+
- **Tools**: Maven (dependency management), Git (version control)
- **Libraries**: Specific dependencies can be added for advanced features.

---

## 📚 Resources and References

- [iNES Format Documentation](https://www.nesdev.org/wiki/INES)
- [6502 CPU Manual](https://www.nesdev.org/wiki/6502)
- [Nesdev Wiki](https://www.nesdev.org/wiki/Main_Page) - An excellent resource for understanding NES hardware.

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

## ✉️ Contact

If you’d like to discuss or contribute to the project, feel free to reach out:

- **Name**: Matheus Oliveira
- **Email**: matheus.6148@gmail.com
- **GitHub**: [omatheusmesmo](https://github.com/omatheusmesmo)
- **LinkedIn**: [Matheus Oliveira](https://www.linkedin.com/in/omatheusmesmo/)

---

I hope this README inspires you to explore the fundamentals of Computer Science and understand how complex systems can be faithfully recreated in software. 🚀
