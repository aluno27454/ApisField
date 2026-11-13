# Arquitetura

## Visão geral

A app segue a arquitetura recomendada pela [documentação Android](https://developer.android.com/topic/architecture): **MVVM com repositórios**. Cada camada só conhece a camada imediatamente abaixo.

```
┌────────────────────────────────────────────────────────────┐
│ UI  Activities / Fragments (Views + XML + ViewBinding)     │
│      observam o StateFlow<UiState> do ViewModel             │
├────────────────────────────────────────────────────────────┤
│ ViewModel  estado do ecrã; chama repositórios em coroutines│
├────────────────────────────────────────────────────────────┤
│ Repository  decide entre API e base local; mapeia erros     │
├──────────────────────────────┬─────────────────────────────┤
│ Remote (Retrofit + OkHttp)   │ Local (Room, SharedPrefs)    │
└──────────────────────────────┴─────────────────────────────┘
        domain/  lógica pura e testável (sem Android)
        sensors/ acesso ao hardware (GPS, bússola, voz, proximidade)
```

## Pacotes

Pacote base: `pt.uptomar.dam2026.apisfield`

| Pacote | Responsabilidade |
|---|---|
| `ui.<ecrã>` | Activities, Fragments, Adapters e ViewModels de cada ecrã (`painel`, `colmeia`, `visitas`, `vespas`, `alertas`, `auth`, `sobre`) |
| `ui.common` | Componentes partilhados: `UiState`, extensões, vistas próprias (`LineChartView`) |
| `data.remote` | `ApiService` (Retrofit), DTOs, `AuthInterceptor`, mapeamento de erros |
| `data.local` | `AppDatabase` (Room), entidades, DAOs, `SessionStore` |
| `data.repository` | Repositórios: a fonte única de dados para os ViewModels |
| `domain` | Modelos e regras: `Validators`, `HiveThresholds`, `Geo` (Haversine), `Triangulation` |
| `sensors` | `LocationProvider`, `CompassSensor`, `ProximityGesture`, `VoiceCommandRecognizer` |
| `di` | `AppContainer`: cria e partilha as dependências (DI manual) |

## Decisões

**Estado do ecrã.** Cada ViewModel expõe um `StateFlow<UiState<T>>`, onde `UiState` é uma sealed class com `Loading`, `Success(data)`, `Empty` e `Error(@StringRes message)`. O Fragment recolhe o fluxo com `repeatOnLifecycle(STARTED)` e mostra a vista certa para cada estado.

**Erros.** Os repositórios convertem exceções em `ApiError`:

| Erro | Comportamento |
|---|---|
| `SemLigacao` | Mostra os dados locais com o aviso "Sem ligação" |
| `NaoAutenticado` (401) | Termina a sessão e volta ao login |
| `SemPermissao` (403) | Mostra uma mensagem de erro |
| `NaoEncontrado` (404) | Mostra uma mensagem de erro |
| `Validacao` (422) | Mostra os erros por campo |
| `Servidor` (5xx) | Mostra uma mensagem de erro |

Nenhuma exceção chega à UI sem tratamento.

**Offline.**
- As visitas e os avistamentos são gravados primeiro no Room, com o estado `PENDENTE`, e o WorkManager envia-os quando houver rede.
- A lista de colmeias fica guardada no Room para leitura sem rede.
- O resto exige ligação.

**Injeção de dependências.** É manual, num `AppContainer` criado no `ApisFieldApp`, como no [guia oficial](https://developer.android.com/training/dependency-injection/manual). Para o tamanho da app não se justifica o Hilt, e assim as dependências ficam explícitas.

**Sensores.** Cada sensor está isolado numa classe em `sensors/`, que regista e anula o registo dos listeners de acordo com o ciclo de vida, para não gastar bateria. Os cálculos (azimute, triangulação, distância) ficam em `domain/`, sem dependências Android, e têm testes unitários.

## Bibliotecas e código de terceiros

As regras da UC permitem bibliotecas, desde que sejam justificadas, que se saiba o que fazem e como se faria sem elas, e que estejam referenciadas. Esta lista é a mesma que aparece no ecrã **Sobre**.

| Biblioteca | Origem | Porquê | Alternativa sem biblioteca |
|---|---|---|---|
| AndroidX Core, AppCompat, Activity, ConstraintLayout | [developer.android.com/jetpack/androidx](https://developer.android.com/jetpack/androidx) | Base do template das aulas | — |
| Material Components | [github.com/material-components](https://github.com/material-components/material-components-android) | Componentes visuais (bottom nav, cartões, campos de texto) | Estilos próprios |
| AndroidX Lifecycle (ViewModel) | [developer.android.com/jetpack/androidx/releases/lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | O estado sobrevive a rotações | `onSaveInstanceState` |
| AndroidX Navigation | [developer.android.com/guide/navigation](https://developer.android.com/guide/navigation) | Navegação da bottom nav | `FragmentManager.replace` |
| Kotlin Coroutines | [github.com/Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Rede e base de dados fora da thread principal | `Executor` + `Handler` |
| Retrofit + Gson converter | [square.github.io/retrofit](https://square.github.io/retrofit/) | Cliente REST declarativo | `HttpURLConnection` + `org.json` |
| OkHttp + logging-interceptor | [square.github.io/okhttp](https://square.github.io/okhttp/) | Token em todos os pedidos, logs em debug | `HttpURLConnection.setRequestProperty` |
| Room | [developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room) | Base de dados local com verificação de SQL na compilação | `SQLiteOpenHelper` |
| WorkManager | [developer.android.com/topic/libraries/architecture/workmanager](https://developer.android.com/topic/libraries/architecture/workmanager) | Envio garantido das visitas pendentes | `ConnectivityManager.NetworkCallback` |
| Coil | [coil-kt.github.io/coil](https://coil-kt.github.io/coil/) | Carregar e reduzir fotos | `BitmapFactory` com `inSampleSize` |
| Play Services Location | [developers.google.com/android/guides/setup](https://developers.google.com/android/guides/setup) | Localização precisa com pouco gasto de bateria | `LocationManager` |
| Material Icons | [fonts.google.com/icons](https://fonts.google.com/icons) | Ícones (Asset Studio) | — |

**Feito à mão:**
- o gráfico de 7 dias (`Canvas`);
- a bússola (`TYPE_ROTATION_VECTOR` + `GeomagneticField`);
- a triangulação (interseção de rumos e mínimos quadrados);
- a distância Haversine;
- o reconhecimento de comandos de voz sobre o `SpeechRecognizer` do Android.

**Não usado (proibido pelas regras):** Firebase e serviços equivalentes.
