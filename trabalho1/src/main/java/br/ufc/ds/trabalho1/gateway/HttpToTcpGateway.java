package br.ufc.ds.trabalho1.gateway;

import br.ufc.ds.trabalho1.rpc.InvestmentReply;
import br.ufc.ds.trabalho1.rpc.InvestmentRequest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpToTcpGateway {
    public static void main(String[] args) throws Exception {
        int httpPort = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String rpcHost = args.length > 1 ? args[1] : "localhost";
        int rpcPort = args.length > 2 ? Integer.parseInt(args[2]) : 7070;

        HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/order", new OrderHandler(rpcHost, rpcPort));
        server.setExecutor(null);
        System.out.println("HTTP->TCP Gateway ouvindo em http://0.0.0.0:" + httpPort + ", encaminhando para " + rpcHost + ":" + rpcPort);
        server.start();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String resp = "OK";
            exchange.sendResponseHeaders(200, resp.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    static class OrderHandler implements HttpHandler {
        private final String rpcHost;
        private final int rpcPort;

        OrderHandler(String rpcHost, int rpcPort) {
            this.rpcHost = rpcHost;
            this.rpcPort = rpcPort;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            byte[] body = readAll(exchange.getRequestBody());
            String s = new String(body, StandardCharsets.UTF_8).trim();

            // Expect JSON like: {"operation":"COMPRA","ticker":"PETR4","quantity":10}
            Map<String, String> map = parseSimpleJson(s);
            String op = map.getOrDefault("operation", "");
            String ticker = map.getOrDefault("ticker", "");
            long qty = 0;
            try { qty = Long.parseLong(map.getOrDefault("quantity", "0")); } catch (Exception ignored){}

            InvestmentReply reply;
            try (Socket sock = new Socket(rpcHost, rpcPort);
                 DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                 DataInputStream in = new DataInputStream(sock.getInputStream())) {

                InvestmentRequest req = new InvestmentRequest(op, ticker, qty);
                byte[] reqPayload = req.toBytes();
                out.writeInt(reqPayload.length);
                out.write(reqPayload);
                out.flush();

                int replySize = in.readInt();
                byte[] replyPayload = new byte[replySize];
                in.readFully(replyPayload);
                reply = InvestmentReply.fromBytes(replyPayload);
            } catch (Exception e) {
                exchange.sendResponseHeaders(502, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(("{\"error\":\"" + e.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String json = "{\"success\":" + reply.isSuccess() + ",\"message\":\"" + escape(reply.getMessage()) + "\",\"unitPrice\":" + reply.getUnitPrice() + ",\"totalValue\":" + reply.getTotalValue() + "}";
            byte[] resp = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        }

        private static byte[] readAll(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
            return baos.toByteArray();
        }

        private static Map<String, String> parseSimpleJson(String s) {
            // very small parser for flat JSON string values/numbers
            java.util.HashMap<String,String> map = new java.util.HashMap<>();
            s = s.trim();
            if (s.startsWith("{") && s.endsWith("}")) s = s.substring(1, s.length()-1);
            String[] parts = s.split(",");
            for (String p: parts) {
                String[] kv = p.split(":",2);
                if (kv.length!=2) continue;
                String k = kv[0].trim();
                if (k.startsWith("\"") && k.endsWith("\"") && k.length()>=2) k = k.substring(1, k.length()-1);
                String v = kv[1].trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length()>=2) v = v.substring(1, v.length()-1);
                map.put(k,v);
            }
            return map;
        }

        private static String escape(String in) {
            return in.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
