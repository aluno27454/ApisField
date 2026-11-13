# ApisField 🐝

Aplicação Android nativa, em Kotlin, para apicultores. Junta a monitorização das colmeias Hive Box a ferramentas de campo que usam os sensores do telemóvel:

- **Painel e detalhe das colmeias:** estado, sensores, alertas e gráficos dos últimos 7 dias.
- **Visitas:** a colmeia mais próxima é detetada por GPS e os quadros são fotografados com a câmara. Funciona sem rede e envia quando houver ligação.
- **Vespa asiática:** registo de avistamentos com foto e localização, e triangulação de ninhos com a bússola.
- **Modo mãos-livres:** comandos de voz, leitura por voz e gesto no sensor de proximidade, para usar com luvas.

Trabalho prático de **Desenvolvimento de Aplicações Móveis**, Licenciatura em Engenharia Informática, Instituto Politécnico de Tomar, ano letivo 2026/27.

## Autores

| Nome | N.º | GitHub |
|---|---|---|
| André Vassalo | 27454 | [@aluno27454](https://github.com/aluno27454) |
| Samuel Ponte | — | [@SamuelPonte](https://github.com/SamuelPonte) |

## Requisitos

- Android Studio (versão estável mais recente) com JDK 17 ou superior
- Android SDK 37; dispositivo ou emulador com Android 10 (API 29) ou superior
- Para a bússola e o sensor de proximidade é preciso um dispositivo real

## Como correr

1. Clonar o repositório e abrir a pasta raiz no Android Studio.
2. Esperar pela sincronização do Gradle.
3. Escolher a configuração `app` e carregar em **Run**.

A versão de debug usa a API local em `http://10.0.2.2:5000` (o `localhost` do computador visto a partir do emulador). A versão de release usa a API alojada no Azure. O código da API está em [ApisField-API](https://github.com/aluno27454/ApisField-API).

```bash
./gradlew assembleDebug   # compila o APK de debug
./gradlew test            # testes unitários
./gradlew lint            # análise estática
```

## Documentação

- [Arquitetura e bibliotecas](docs/ARQUITETURA.md)
- [Como contribuir](CONTRIBUTING.md): fluxo de trabalho, branches, commits e revisão

## Planeamento

O trabalho está organizado em [issues](https://github.com/aluno27454/ApisField/issues) agrupadas por [milestones](https://github.com/aluno27454/ApisField/milestones):

1. **Fundação:** projeto, rede, autenticação, painel e base local.
2. **Funcionalidades de campo:** visitas, vespas, triangulação e mãos-livres.
3. **Polimento e publicação:** qualidade, Google Play e relatório.
