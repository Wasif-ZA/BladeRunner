package CCP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

import org.json.JSONObject;

// Drives a real CCP over localhost UDP with a stand-in MCP and ESP32; exits non-zero on failure.
public class CCPSelfTest {

    static final InetAddress LOCAL = InetAddress.getLoopbackAddress();
    static int passed = 0;

    public static void main(String[] args) throws Exception {
        check("raw ACK accepted", CCP.isAck("ACK"));
        check("JSON ACK accepted", CCP.isAck("{\"message\":\"ACK\"}"));
        check("other text rejected", !CCP.isAck("NOPE"));

        DatagramSocket mcp = new DatagramSocket(0, LOCAL);
        DatagramSocket esp = new DatagramSocket(0, LOCAL);
        mcp.setSoTimeout(200);
        esp.setSoTimeout(200);
        ConcurrentLinkedQueue<String> fromCcp = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> atEsp = new ConcurrentLinkedQueue<>();
        startReceiver(mcp, fromCcp, false);
        startReceiver(esp, atEsp, true);

        int ccpPort = freePort();
        int espListenPort = freePort();
        CCP ccp = new CCP("BR12", "127.0.0.1", mcp.getLocalPort(), "127.0.0.1", ccpPort,
                "127.0.0.1", esp.getLocalPort(), espListenPort, 1);

        ccp.connect();
        check("connect sets CONNECTED", ccp.getState() == StateManager.CCPState.CONNECTED);
        waitFor("MCP receives CCIN", () -> fromCcp.stream().anyMatch(m -> m.contains("\"CCIN\"")));

        sendTo(mcp, ccpPort, msg("AKIN", null));
        Thread.sleep(300);
        check("AKIN keeps CONNECTED", ccp.getState() == StateManager.CCPState.CONNECTED);

        sendTo(mcp, ccpPort, "not json");
        exec(mcp, ccpPort, "FFASTC");
        waitFor("FFASTC after garbage -> FULL_SPEED", () -> ccp.getState() == StateManager.CCPState.FULL_SPEED);
        waitFor("ESP receives forwarded FFASTC", () -> atEsp.stream().anyMatch(m -> m.contains("FFASTC")));

        exec(mcp, ccpPort, "STOPC");
        waitFor("STOPC from FULL_SPEED -> STOPPED", () -> ccp.getState() == StateManager.CCPState.STOPPED);

        exec(mcp, ccpPort, "STOPO");
        waitFor("STOPO opens doors", ccp::areDoorsOpen);
        check("STOPO stays STOPPED", ccp.getState() == StateManager.CCPState.STOPPED);

        exec(mcp, ccpPort, "STOPC");
        waitFor("STOPC closes doors", () -> !ccp.areDoorsOpen());

        exec(mcp, ccpPort, "FSLOWC");
        waitFor("FSLOWC -> SLOW_FORWARD", () -> ccp.getState() == StateManager.CCPState.SLOW_FORWARD);

        ccp.setAlignedWithPhotodiode(true);
        exec(mcp, ccpPort, "RSLOWC");
        waitFor("RSLOWC when aligned -> STOPPED", () -> ccp.getState() == StateManager.CCPState.STOPPED);
        ccp.setAlignedWithPhotodiode(false);

        exec(mcp, ccpPort, "FFASTC");
        waitFor("FFASTC again -> FULL_SPEED", () -> ccp.getState() == StateManager.CCPState.FULL_SPEED);
        sendTo(esp, espListenPort, "not json");
        sendTo(esp, espListenPort, new JSONObject().put("action", "HAZARD_DETECTED").toString());
        waitFor("ESP hazard -> STOPPED", () -> ccp.getState() == StateManager.CCPState.STOPPED);
        waitFor("ESP receives HAZARD_STOPPED", () -> atEsp.stream().anyMatch(m -> m.contains("HAZARD_STOPPED")));

        System.out.println("CCPSelfTest: " + passed + " checks passed");
        System.exit(0);
    }

    static String msg(String type, String action) {
        JSONObject o = new JSONObject().put("client_type", "MCP").put("message", type).put("client_id", "BR12");
        if (action != null) {
            o.put("action", action);
        }
        return o.toString();
    }

    static void exec(DatagramSocket from, int port, String action) throws Exception {
        sendTo(from, port, msg("EXEC", action));
    }

    static void sendTo(DatagramSocket from, int port, String text) throws Exception {
        byte[] b = text.getBytes();
        from.send(new DatagramPacket(b, b.length, LOCAL, port));
    }

    static void startReceiver(DatagramSocket s, ConcurrentLinkedQueue<String> sink, boolean ack) {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[2048];
            while (true) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    s.receive(p);
                    String text = new String(p.getData(), 0, p.getLength());
                    sink.add(text);
                    if (ack && text.contains("\"sequence\"")) {
                        byte[] a = "{\"message\":\"ACK\"}".getBytes();
                        s.send(new DatagramPacket(a, a.length, p.getAddress(), p.getPort()));
                    }
                } catch (SocketTimeoutException e) {
                    // poll again
                } catch (Exception e) {
                    return;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static int freePort() throws Exception {
        try (DatagramSocket s = new DatagramSocket(0, LOCAL)) {
            return s.getLocalPort();
        }
    }

    static void waitFor(String name, BooleanSupplier cond) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (cond.getAsBoolean()) {
                check(name, true);
                return;
            }
            Thread.sleep(20);
        }
        check(name, false);
    }

    static void check(String name, boolean ok) {
        if (!ok) {
            System.err.println("FAIL: " + name);
            System.exit(1);
        }
        passed++;
        System.out.println("ok: " + name);
    }
}
