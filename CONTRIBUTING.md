# Como contribuir

Este documento define como trabalhamos no repositório. O objetivo é que qualquer pessoa consiga perceber o histórico e continuar o trabalho.

## Fluxo de trabalho

1. **Issue primeiro.** Todo o trabalho começa numa issue, com contexto, critérios de aceitação e uma pessoa atribuída.
2. **Branch a partir de `main`**, com o número da issue:
   - `feat/12-formulario-visita`: funcionalidade nova
   - `fix/31-crash-sem-gps`: correção de erro
   - `refactor/…`, `test/…`, `docs/…`, `chore/…`: o resto
3. **Commits pequenos**, cada um com uma única intenção (ver abaixo).
4. **Pull request** para `main`, preenchendo o template. Na descrição, escrever `Closes #<n.º>` para fechar a issue automaticamente.
5. **Revisão:** o outro membro do grupo revê o PR. O CI tem de passar.
6. **Squash merge.** Depois do merge, apagar a branch.

Ninguém faz push direto para `main`.

## Mensagens de commit

Seguimos o formato [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/), escrito em português:

```
<tipo>(<âmbito>): <descrição no imperativo, minúsculas, sem ponto final>

[corpo opcional: o porquê da alteração]
```

| Tipo | Quando usar |
|---|---|
| `feat` | Funcionalidade nova |
| `fix` | Correção de erro |
| `refactor` | Alteração interna sem mudar o comportamento |
| `test` | Testes |
| `docs` | Documentação |
| `style` | Formatação |
| `chore` | Build, CI, dependências |

Âmbitos mais usados: `painel`, `colmeias`, `visitas`, `vespas`, `triangulacao`, `alertas`, `maos-livres`, `sobre`, `auth`, `rede`, `dados`, `ui`, `ci`.

Exemplo: `feat(visitas): pré-selecionar a colmeia mais próxima por GPS`

## Estilo de código

- Seguir as [convenções de código do Kotlin](https://kotlinlang.org/docs/coding-conventions.html). A formatação está definida no `.editorconfig`: antes de cada commit, usar **Code → Reformat Code** no Android Studio.
- **Comentários obrigatórios:**
  - KDoc (`/** … */`) em todas as classes e funções públicas;
  - comentários de linha onde a lógica não for óbvia (fórmulas, limiares, casos especiais).
  - Os comentários explicam o *porquê*, não repetem o código.
- **Código de terceiros:** quando se usar uma biblioteca ou um excerto de código de outra origem, pôr um comentário com a origem (URL) junto do código e acrescentá-la ao ecrã **Sobre** e a [docs/ARQUITETURA.md](docs/ARQUITETURA.md). É uma regra da UC: código sem referência é considerado plágio.
- **Textos:** nenhum texto visível ao utilizador fica no código. Vão todos para `res/values/strings.xml`, em português europeu (AO90), tratando o utilizador por "tu".
- **Nomes:**
  - classes em `PascalCase`, funções e variáveis em `camelCase`, constantes em `UPPER_SNAKE_CASE`;
  - layouts com o tipo primeiro: `activity_login.xml`, `fragment_painel.xml`, `item_colmeia.xml`;
  - IDs de views em `camelCase` (`btnGuardar`, `etNotas`).

## Antes de abrir um PR

- [ ] `./gradlew lint test assembleDebug` passa sem erros
- [ ] Testado num dispositivo ou emulador (bússola, voz e proximidade só num dispositivo real)
- [ ] Sem textos no código; strings novas em `strings.xml`
- [ ] KDoc e comentários nas partes novas
- [ ] Bibliotecas novas justificadas e referenciadas (Sobre + ARQUITETURA.md)

## Revisão

Quem revê confirma que:

- o código cumpre os critérios de aceitação da issue;
- percebe o código ao ponto de o conseguir explicar na defesa;
- os erros são tratados e há validação nos formulários.

Os comentários de revisão são sugestões concretas. Quem abriu o PR responde a cada um ou resolve-o.
