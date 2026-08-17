# My Application

A Spring Boot + Vaadin project. Build your UI in pure Java — no HTML, no JavaScript.

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

The single piece of logic in this project — deciding whether the name you typed is
a capitalized word, a number, or something else, and colouring the notification
accordingly — is covered five times over, once at every level of the test
pyramid. Each level buys a different kind of confidence at a different price.

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

| # | Level | Test class | Technology | What it proves |
|---|-------|-----------|------------|----------------|
| 1 | Unit | [`NameClassifierTest`](src/test/java/dev/example/testing/logic/NameClassifierTest.java) | JUnit only | The classification rules themselves, including Unicode edge cases |
| 2 | Integration | [`HelloWorldViewIntegrationTest`](src/test/java/dev/example/testing/it/HelloWorldViewIntegrationTest.java) | `@SpringBootTest` | The application context boots and Spring can construct the view (i.e. its dependencies can be injected) |
| 3 | UI unit | [`HelloWorldViewBrowserlessTest`](src/test/java/dev/example/testing/ui/HelloWorldViewBrowserlessTest.java) | Vaadin [browserless testing](https://vaadin.com/docs/latest/flow/testing/browserless) | Every branch of the click listener, driven through the real Vaadin server-side API — no browser, no servlet container |
| 4 | End-to-end | [`HelloWorldViewIT`](src/test/java/dev/example/testing/ui/HelloWorldViewIT.java) | Vaadin [TestBench](https://vaadin.com/docs/latest/flow/testing/end-to-end) | The whole stack in a real browser, using an API that understands Vaadin components and waits for client-server round trips automatically |
| 5 | End-to-end | [`HelloWorldViewPlaywrightIT`](src/test/java/dev/example/testing/ui/HelloWorldViewPlaywrightIT.java) | [Playwright](https://playwright.dev/java/) | The same, but with a generic browser automation library talking to the web components directly |

The three UI-related test classes sit in `dev.example.testing.ui`, next to the
view they exercise, and all three locate components the same way: the view calls
`setTestId("name-field")`, which renders as a `data-testid` attribute that
browserless (`find(TextField.class).testId(...)`) and Playwright
(`[data-testid='name-field']`) can both target. Prefer that over CSS selectors or
text content — test IDs survive restyling, relayouting and translation.

Levels 1–3 run in about three seconds in total and are meant for every save.
Levels 4 and 5 need a real browser, so they are named `*IT` and kept behind a
Maven profile.

### Running the tests

Fast feedback loop — levels 1, 2 and 3:

```bash
./mvnw test
```

Everything, including the browser-based end-to-end tests:

```bash
./mvnw verify -Pe2e
```

A single level, or a single test method:

```bash
./mvnw test -Dtest=NameClassifierTest                       # level 1
./mvnw test -Dtest=HelloWorldViewIntegrationTest            # level 2
./mvnw test -Dtest=HelloWorldViewBrowserlessTest            # level 3
./mvnw verify -Pe2e -Dit.test=HelloWorldViewIT              # level 4
./mvnw verify -Pe2e -Dit.test=HelloWorldViewPlaywrightIT    # level 5

./mvnw test -Dtest='HelloWorldViewBrowserlessTest#greeting_usesMatchingThemeVariant'
```

The Playwright tests are tagged `playwright`, so they can be filtered. This is
the useful recipe for a machine without a Vaadin subscription — it runs the
end-to-end level that needs no licence and leaves out the one that does:

```bash
./mvnw verify -Pe2e -Dgroups=playwright              # only level 5
./mvnw verify -Pe2e -DexcludedGroups=playwright      # everything but level 5
```

Two things worth knowing about these filters:

- The tag filter applies to the unit tests as well, so `-Dgroups=playwright`
  reports `Tests run: 0` for levels 1–3. Combine it with a separate `./mvnw test`
  run if you want both.
- If a filter ends up matching no integration test at all, the `verify` goal
  fails with an empty test run. Add `-DfailIfNoTests=false` when that is
  intentional.

### What you need for the end-to-end levels

Both end-to-end levels start the application themselves on a random free port
via `@SpringBootTest(webEnvironment = RANDOM_PORT)`, so you do **not** have to
start a server first — and a development server on port 8080 will not interfere.

**Level 4 (TestBench)** requires a **commercial Vaadin subscription**; TestBench
validates its licence at runtime and the tests error out without one. Start a
[free trial](https://vaadin.com/trial) or sign in once so that a licence key is
stored in `~/.vaadin/proKey`. It also needs a local Chrome.

**Level 5 (Playwright)** needs a browser too, but downloads one itself on first
run. To install it explicitly:

```bash
./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.classpathScope=test -Dexec.args="install chromium"
```

On a bare Linux machine you may also need the browser's system libraries:

```bash
sudo ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.classpathScope=test -Dexec.args="install-deps"
```

(`-Dexec.classpathScope=test` is required because Playwright is a test-scoped
dependency.)

This means that in a CI pipeline without a Vaadin subscription, levels 1, 2, 3
and 5 all run; only level 4 needs to be skipped.

### Where to add tests next

As the application grows, each new piece of behaviour belongs at the lowest level
that can still prove it:

- **Business rules** → level 1. No framework, no context, milliseconds per test.
- **Services, repositories, database access** → level 2, with `@SpringBootTest`,
  `@DataJpaTest` or Testcontainers. This is also where HTTP-level checks belong
  once they matter: that `/` returns the bootstrap page, that a custom stylesheet
  is served, that the `@Push` endpoint answers.
- **View behaviour** → level 3. It is fast enough to cover every branch, and
  because it runs in the same JVM as the server you can seed and roll back the
  database from the test.
- **Critical user journeys only** → levels 4 and 5. Keep these few; they are the
  slowest and the most likely to fail for reasons unrelated to your change.

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
