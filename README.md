<p align="center">
  <a href="#quickstart"><strong>Quickstart</strong></a> &middot;
  <a href="#how-it-works"><strong>How it works</strong></a> &middot;
  <a href="#run-the-control-processors"><strong>Control processors</strong></a> &middot;
  <a href="#docs"><strong>Docs</strong></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/firmware-ESP32-E7352C" alt="ESP32 firmware">
  <img src="https://img.shields.io/badge/control-Java%2011%2B-007396" alt="Java 11+">
  <img src="https://img.shields.io/badge/UI-Next.js%2016-000000" alt="Next.js 16">
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="MIT">
</p>

<br>

# BladeRunner drives a model carriage from a web page, through Java, down to an ESP32.

A carriage control prototype: firmware, control processors and an operator UI in one repo.

**If the carriage is the _train_, BladeRunner is the _signal box_.**

A master control processor (MCP) sends a command. The carriage control processor (CCP) checks it
against the carriage state and passes it to the ESP32 over UDP. The ESP32 moves the carriage and
reports back. Every message is JSON.

**See the whole control chain in one repo, not three.**

|        | Step              | Example                                           |
| ------ | ----------------- | ------------------------------------------------- |
| **01** | Run the UI        | _`npm --prefix website/code ci`, then `run dev`_  |
| **02** | Self-test the CCP | _`java ... CCP.CCPSelfTest`_                      |
| **03** | Watch them talk   | _ESP32 stand-in, CCP and MCP in three terminals_  |

> [!TIP]
> **🚆 Or hand it to your agent.** Paste this into Claude Code or Codex:
>
> ```text
> Clone https://github.com/Wasif-ZA/BladeRunner. Run `npm --prefix website/code ci` and
> `npm --prefix website/code run dev`, then compile and run the CCP self-test from the README.
> ```

<br>

## Problems BladeRunner solves

| Without BladeRunner | With BladeRunner |
| --- | --- |
| ❌ The hardware works, but nobody can follow the project without the lab. | ✅ The CCP self-test runs on localhost with a stand-in MCP and ESP32. |
| ❌ Control logic is tangled into firmware. | ✅ State transitions live in a Java state manager you can test on a laptop. |
| ❌ Messages are ad hoc strings. | ✅ Commands are encoded and decoded as JSON. |
| ❌ Lab IP addresses are baked into the code. | ✅ Defaults are `127.0.0.1`; lab addresses are command-line arguments. |

<br>

## How it works

```
[Operator]
    │
    ▼
Next.js UI (website/code)        in-browser model of the CCP state machine
    │
    ▼
MCP  ──UDP 3000──▶  CCP  ──UDP 3001──▶  ESP32 firmware (ESP/)
                                            │
                                            ▼
                               carriage, doors, sensors
```

<details>
<summary>Open: what each part does</summary>

| Part | Folder | What it does |
| --- | --- | --- |
| Operations UI | `website/code/` | Next.js 16, React 19, Tailwind. Models the CCP state machine in the browser; it does not connect to the Java processes |
| Control processors | `CCP/` | Java. Command parsing, the carriage state manager, UDP handlers, `org.json` from a local JAR |
| Firmware | `ESP/`, `Carriage/`, `MVP/` | ESP32 sketches. Built with the Arduino IDE or PlatformIO, not by CI |
| Older copy | `enggg/` | An earlier CCP and the `TestESPServer` stand-in |

CI typechecks and builds the UI, and compiles the CCP and runs its self-test.

</details>

<br>

## Quickstart

Node.js 20+ and Java JDK 11+.

```bash
git clone https://github.com/Wasif-ZA/BladeRunner
cd BladeRunner
npm --prefix website/code ci
npm --prefix website/code run dev
```

Open http://localhost:3000. No environment variables are needed.

## Run the control processors

Self-test (on Windows use `;` instead of `:` in `-cp`):

```bash
javac -d out -cp CCP/lib/json-20240303.jar CCP/*.java
java -cp "out:CCP/lib/json-20240303.jar" CCP.CCPSelfTest
```

It starts a real CCP with a stand-in MCP and ESP32 and checks connect, FFASTC, STOPC, STOPO,
FSLOWC, RSLOWC and the ESP hazard stop.

<details>
<summary>Open: watch the three processes over UDP</summary>

Three terminals, in this order:

```bash
javac -d out -cp enggg/lib/json-20240303.jar enggg/TestESPServer.java
java -cp "out:enggg/lib/json-20240303.jar" TestESPServer   # ESP32 stand-in on UDP 3001
java -cp "out:CCP/lib/json-20240303.jar" CCP.CCP           # CCP on UDP 3000
java -cp "out:CCP/lib/json-20240303.jar" CCP.MCP           # MCP on UDP 2000, sends FFASTC
```

On the lab network, pass the addresses:

```bash
# CCP args: mcpHost mcpPort ccpHost ccpPort espHost espPort espListenPort
java -cp "out:CCP/lib/json-20240303.jar" CCP.CCP 10.20.30.187 3012 10.20.30.153 3000 10.20.30.112 3012 3012
# MCP args: ccpHost ccpPort
java -cp "out:CCP/lib/json-20240303.jar" CCP.MCP 10.20.30.153 3000
```

</details>

## Roadmap

- ⬜ Turn the UI into a real command console wired to the CCP
- ⬜ One shared message schema for processors and firmware
- ⬜ Screenshots or a demo recording

## Docs

1. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): the layers and how they connect.
2. [`docs/DECISIONS.md`](docs/DECISIONS.md): engineering decisions.
3. [`docs/PROJECT_OVERVIEW.md`](docs/PROJECT_OVERVIEW.md): the project at a glance.

Tests: `npm --prefix website/code run test`, plus the CCP self-test above.

## License

MIT. See [`LICENSE`](LICENSE).
