# SelfMat-NES-Emulator

![Java](https://img.shields.io/badge/Java-21%2B-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 🎮 Proposta do Projeto

Este projeto é um emulador de **Nintendo Entertainment System (NES)** desenvolvido em Java. O objetivo é recriar o funcionamento do hardware do NES em software, permitindo que jogos clássicos sejam executados em plataformas modernas. O emulador implementa componentes essenciais do NES, como:

- **CPU (6502)**: Simulação do processador principal do NES.
- **PPU (Picture Processing Unit)**: Renderização de gráficos e sprites.
- **APU (Audio Processing Unit)**: Geração de áudio.
- **Mappers**: Tradução de endereços de memória para suportar diferentes cartuchos de jogos.

Além de ser uma homenagem aos clássicos dos videogames, este projeto demonstra como conceitos fundamentais da Ciência da Computação podem ser aplicados para construir sistemas complexos e funcionais.

---

## 🎓 Emulação e a Ciência da Computação

Desenvolver um emulador de NES em Java é um desafio que exige o domínio de muitos conceitos fundamentais da Ciência da Computação. Este projeto destaca a relevância de compreender e aplicar áreas-chave como:

### 1. **Arquitetura de Computadores**
   - Compreensão detalhada de como uma CPU opera, abrangendo ciclos de instrução, registradores e modos de endereçamento.
   - Simulação de sistemas de memória hierárquicos (RAM, ROM, VRAM, etc.), essenciais para replicar o funcionamento do NES.

### 2. **Programação de Baixo Nível**
   - Manipulação direta de bits e bytes para implementar operações da CPU e dos mappers.
   - Interpretação de arquivos `.nes` em formatos binários e hexadecimais, aproximando-se da lógica original do hardware.

### 3. **Concorrência e Sincronização**
   - Coordenação entre componentes independentes, como a CPU, PPU e APU, que operam em paralelo e precisam estar sincronizados para garantir o funcionamento correto dos jogos.

### 4. **Emulação de Hardware**
   - Recriação detalhada de componentes como o PPU, responsável por gráficos, e o APU, que gera áudio.
   - Implementação de mappers, necessários para lidar com diferentes layouts de memória e suportar variações de cartuchos.

### 5. **Desenvolvimento de Software**
   - Aplicação de boas práticas, como modularização, testes e documentação, para gerenciar sistemas com alta complexidade.
   - Uso de algoritmos e estruturas de dados otimizados para garantir que o emulador funcione de forma eficiente.

### 6. **Domínio de Java**
   - Utilização avançada da linguagem Java para criar sistemas robustos, envolvendo manipulação de arquivos binários, programação orientada a objetos e integração de componentes em tempo real.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 21+
- **Ferramentas**: Maven (gerenciamento de dependências), Git (controle de versão)
- **Bibliotecas**: Dependências específicas podem ser adicionadas para funcionalidades avançadas.

---

## 📚 Recursos e Referências

- [Documentação do iNES Format](https://www.nesdev.org/wiki/INES)
- [Manual da CPU 6502](https://www.nesdev.org/wiki/6502)
- [Nesdev Wiki](https://www.nesdev.org/wiki/Main_Page) - Excelente fonte para compreender o hardware do NES.

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT.

---

## ✉️ Contato

Se quiser discutir ou contribuir para o projeto, entre em contato:

- **Nome**: Matheus Oliveira
- **Email**: matheus.6148@gmail.com
- **GitHub**: [omatheusmesmo](https://github.com/omatheusmesmo)
- **LinkedIn**: [Matheus Oliveira](https://www.linkedin.com/in/omatheusmesmo/)

---

Espero que este README inspire você a explorar os fundamentos da Ciência da Computação e a compreender como sistemas complexos podem ser recriados de forma funcional. 🚀
