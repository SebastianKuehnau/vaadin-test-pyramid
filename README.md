# Vaadin Test Pyramid Demo Application

A Spring Boot + Vaadin project with Testbench, JUnit, Playwright and BrowserlessTest Cases. Build and Test your UI in pure Java.

> **New to Vaadin?** The 5-minute [Quickstart](https://vaadin.com/quickstart) walks you from here to your first running app, a live code change, and an AI-assisted edit with Copilot.

---

## Fastest start — no plugin needed

From the project folder:

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Then open **http://localhost:8080**.

The first start takes ~30 seconds while Maven downloads dependencies.

> **Port 8080 already in use?** Stop the other process, or set `server.port=8081` in `src/main/resources/application.properties` and open that port instead.
>
> **To stop the app:** press `Ctrl+C` in the terminal (or the red Stop button if you launched from your IDE).

## Optional upgrade — instant hotswap

Running with `spring-boot:run` works, but Java code changes need a server restart. For **live reload** — edit Java, see it in the browser without restarting — install the **Vaadin plugin** and start the app through it:

- **IntelliJ IDEA:** install *Vaadin* from the JetBrains Marketplace → **Debug using Hotswap Agent** (dropdown next to Run). *Just installed it? Let IntelliJ finish indexing, or restart it, if the menu item isn't there yet.*
- **VS Code:** install the *Vaadin* extension → **Vaadin: Debug using Hotswap Agent** from the command palette.
- **Eclipse:** install the *Vaadin* plugin → right-click the project → **Run As → Vaadin Application**.

This is what makes the edit-and-see-it loop feel instant — and it's required for the AI edits in [Vaadin Copilot](https://vaadin.com/docs/latest/tools/copilot).

---

## Ask your AI assistant about Vaadin (optional)

If you use Claude Code, Cursor, or another AI coding assistant, connect it to the **Vaadin MCP server** so it answers against real Vaadin docs and the exact API of your installed version — instead of guessing from outdated training data.

```bash
# One-time setup — see https://vaadin.com/docs/latest/building-apps/mcp
```

A `.mcp.json` is included (commented out by default). Uncomment it, or run the setup command above, to activate.

---

## Testing — the test pyramid

One piece of logic — is the name a capitalized word, a number, or something else,
and which notification variant does that produce — tested five times, once per
level of the pyramid. Each level buys a different kind of confidence at a
different price.

```
                     ▲  slow, brittle, closest to a real user
        ┌────────────┴────────────┐
        │  5  Playwright (E2E)    │   real browser, generic API
        ├─────────────────────────┤
        │  4  TestBench (E2E)     │   real browser, Vaadin-aware API
        ├─────────────────────────┤
        │  3  Browserless UI test │   real Vaadin components, no browser
        ├─────────────────────────┤
        │  2  Spring integration  │   real application context
        ├─────────────────────────┤
        │  1  Unit test           │   plain Java, no framework
        └────────────┬────────────┘
                     ▼  fast, stable, furthest from a real user
```

| # | Test class | Technology | What it proves |
|---|-----------|------------|----------------|
| 1 | [`NameClassifierTest`](src/test/java/dev/example/testing/logic/NameClassifierTest.java) | JUnit only | The classification rules |
| 2 | [`HelloWorldViewIntegrationTest`](src/test/java/dev/example/testing/it/HelloWorldViewIntegrationTest.java) | `@SpringBootTest` | The context boots and Spring can construct the view |
| 3 | [`HelloWorldViewBrowserlessTest`](src/test/java/dev/example/testing/ui/HelloWorldViewBrowserlessTest.java) | [Browserless testing](https://vaadin.com/docs/latest/flow/testing/browserless) | The click behaviour, via the real Vaadin server-side API — no browser, no servlet container |
| 4 | [`HelloWorldViewIT`](src/test/java/dev/example/testing/ui/HelloWorldViewIT.java) | [TestBench](https://vaadin.com/docs/latest/flow/testing/end-to-end) | The whole stack in a browser, with an API that knows Vaadin components |
| 5 | [`HelloWorldViewPlaywrightIT`](src/test/java/dev/example/testing/ui/HelloWorldViewPlaywrightIT.java) | [Playwright](https://playwright.dev/java/) | The same, with a library that knows nothing about Vaadin |

Levels 3, 4 and 5 run the same three cases so they can be compared side by side,
and all three find components by test ID: the view calls
`setTestId("name-field")`, which renders as `data-testid` and is targeted by
`find(TextField.class).testId(...)` in browserless and
`[data-testid='name-field']` in Playwright. Prefer that over CSS selectors or
text content — test IDs survive restyling and translation.

### Running the tests

```bash
./mvnw test                              # levels 1–3, ~3 seconds
./mvnw verify -Pe2e                      # all five levels
./mvnw verify -Pe2e -Dgroups=playwright  # add level 5 only, no licence needed
```

Levels 4 and 5 are named `*IT`, so Surefire skips them and only the `e2e` profile
(Failsafe) picks them up. Both start the application themselves on a random free
port, so no server has to be running and port 8080 stays free for development.

Single class or method:

```bash
./mvnw test -Dtest=HelloWorldViewBrowserlessTest
./mvnw test -Dtest='HelloWorldViewBrowserlessTest#greeting_usesMatchingThemeVariant'
./mvnw verify -Pe2e -Dit.test=HelloWorldViewPlaywrightIT
```

Note that `-Dgroups`/`-DexcludedGroups` also filter levels 1–3, and that `verify`
fails if a filter matches no integration test at all — add `-DfailIfNoTests=false`
when that is intentional.

### Prerequisites for levels 4 and 5

- **TestBench** requires a **commercial Vaadin subscription** (validated at
  runtime; without it the tests error out) and a local Chrome. Start a
  [free trial](https://vaadin.com/trial) to get a key in `~/.vaadin/proKey`.
- **Playwright** downloads a browser on first run. To install it — and, on a bare
  Linux machine, its system libraries — explicitly instead
  (`-Dexec.classpathScope=test` is required because Playwright is a test-scoped
  dependency):

  ```bash
  ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.classpathScope=test -Dexec.args="install chromium"
  sudo ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.classpathScope=test -Dexec.args="install-deps"
  ```

So in CI without a Vaadin subscription, only level 4 needs to be skipped.

### Where to add tests next

Put each new test at the lowest level that can still catch the bug it targets:

- **Business rules** → level 1.
- **Services, repositories, HTTP endpoints** → level 2, with `@DataJpaTest`,
  `@WebMvcTest` or Testcontainers.
- **View behaviour and validation** → level 3. Fast enough to cover every branch,
  and it can seed and roll back the database from the same JVM.
- **A handful of critical journeys and anything visual** → levels 4 and 5.

## Build for production

```bash
./mvnw package
java -jar target/*.jar
```

## Learn more

- [Vaadin Quickstart](https://vaadin.com/quickstart) — the 5-minute getting-started path
- [Components](https://vaadin.com/docs/latest/components) — 50+ UI components, all callable from Java
- [Vaadin Copilot](https://vaadin.com/docs/latest/tools/copilot) — visual + AI editing in the browser
- [Full documentation](https://vaadin.com/docs)
