# BladeRunner ops UI

Next.js 16 site that explains the BladeRunner system and includes an in-browser model of the
`CCP.java` state machine ("Live Simulation" section). The model runs entirely in the browser; it
does not talk to the Java CCP or MCP over the network.

## Commands

```bash
npm ci
npm run dev        # http://localhost:3000
npm run build
npm run start
npm run lint       # tsc --noEmit
npm run test       # tsc --noEmit
npm run format     # trims trailing whitespace and writes LF line endings
```

Source lives in `src/app/`; the page is assembled in `src/app/components/BladeRunnerPage.tsx`.
No environment variables are needed.
