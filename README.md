# DigitBoard ⌨️📱
> Teclado Virtual Neumorphism (Soft UI) de Alta Performance para Android com Compilação CI/CD em APK via GitHub Actions.

![DigitBoard Header](https://img.shields.io/badge/DigitBoard-v1.0.0-4d6bfe?style=for-the-badge&logo=android)
![CI/CD Build](https://img.shields.io/badge/GitHub_Actions-CI/CD_APK-brightgreen?style=for-the-badge&logo=githubactions)
![UI Style](https://img.shields.io/badge/UI_Design-Neumorphism_Soft_UI-e0e5ec?style=for-the-badge)

---

## 🌟 Visão Geral

O **DigitBoard** é um teclado virtual completo desenvolvido para Android, projetado com inspiração no Gboard e em concorrentes do mercado. Ele combina uma interface nativa elegante com suporte a temas modernos, atalhos de produtividade e compilação automatizada via CI/CD no GitHub.

---

## 🎨 Temas Incluídos

1. **Neumorphism (Soft UI)**:
   - **Cor Base**: `#e0e5ec`
   - **Sombras**: Dupla sombra 3D (`9px 9px 18px #bec3c9, -9px -9px 18px #ffffff`)
   - **Estados Pressionados**: Inset sombras internas
   - **Bordas**: Cantos suaves de `16px` a `24px`
   - **Accent**: `#4d6bfe`
   - **Tipografia**: `#2d3436` de alto contraste

2. **Escuro AMOLED**:
   - Fundo `#000000` preto puro focado em economia de bateria e telas OLED/AMOLED.

3. **Claro / Branco**:
   - Estilo clean `#F8F9FA` clássico e de altíssima visibilidade.

4. **RGB Animado (Chroma)**:
   - Bordas e brilho neon com gradiente colorido gamer em constante rotação.

---

## ⚡ Principais Funcionalidades (Padrão Gboard)

- **Layout QWERTY & ABNT2**: Suporte completo a acentuação estendida (pressão longa em teclas `a, e, i, o, u, c, n`).
- **Linha Numérica Superior (0-9)**: Ativável/desativável nas configurações.
- **Barra de Sugestões Inteligente**: Predição de palavras em tempo real para Português (BR) e Inglês.
- **Digitação por Gestos (Glide / Swipe)**: Rastreamento do rastro do dedo para digitação rápida.
- **Painel de Emojis Organizado**: Seleção rápida de emojis por categorias.
- **Gerenciador de Área de Transferência**: Histórico de textos copiados e atalho de cola rápida.
- **Edição de Texto de Precisão**: Painel direcional com setas (cima, baixo, esquerda, direita, início, fim, selecionar tudo, copiar e colar).
- **Entrada por Voz / Ditado**: Atalho rápido para acionamento do microfone.
- **Feedback Tátil & Sonoro**: Vibração hápica e sons de clique personalizáveis.
- **Pop-up de Tecla**: Pré-visualização ampliada do caractere ao tocar.

---

## 📦 Como Baixar o APK Compilado (GitHub CI/CD)

O repositório está configurado com **GitHub Actions** para compilar o código Kotlin nativo e gerar o arquivo APK automaticamente a cada commit ou por acionamento manual.

1. Acesse a aba **[Actions](../../actions)** no topo do repositório.
2. Selecione a última execução do workflow **Build DigitBoard APK CI/CD**.
3. Na seção **Artifacts**, clique em `DigitBoard-APKs` para baixar o arquivo ZIP contendo os APKs.
4. Alternativamente, acesse a aba **[Releases](../../releases)** para baixar a versão mais recente diretamente para o seu dispositivo Android.

---

## 🛠️ Como Executar o Simulador e Preview Web

O projeto conta com um servidor interativo de pré-visualização web rodando no ambiente de sandbox:

```bash
cd preview
npm install
npm run dev
```

Abra a URL de preview gerada no navegador para testar interativamente todos os 4 temas, layouts, recursos de digitação, gestos e atalhos em tempo real.

---

## 📂 Estrutura do Repositório

```
DigitBoard/
├── .github/workflows/
│   └── build-apk.yml               # Workflow do GitHub Actions para compilação do APK
├── app/
│   ├── build.gradle.kts            # Configuração Gradle do módulo Android
│   └── src/main/
│       ├── AndroidManifest.xml     # Registro do InputMethodService e Activity de Configurações
│       ├── java/com/digitboard/keyboard/
│       │   ├── DigitBoardIME.kt    # Código-fonte principal do Teclado Virtual (IME)
│       │   ├── SettingsActivity.kt # Interface Neumorphic para ativação e opções
│       │   └── service/            # Mecanismos de sugestão, áudio e hápter
│       └── res/                    # Estilos, Cores Neumorphism/AMOLED/RGB e Layouts XML
├── preview/                        # Aplicação Web para Preview Interativo ao Vivo
└── README.md
```

---

© 2026 DigitBoard • Teclado Virtual Neumorphism com Build APK Automatizado
