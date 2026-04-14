package br.ufc.ds.trabalho1.investment.demo;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;
import br.ufc.ds.trabalho1.investment.stream.OrdemInvestimentoInputStream;
import br.ufc.ds.trabalho1.investment.stream.OrdemInvestimentoOutputStream;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class StreamExerciseDemo {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "out-stdout" -> outputToStdout();
            case "out-file" -> outputToFile(args.length > 1 ? args[1] : "ordens.bin");
            case "in-file" -> inputFromFile(args.length > 1 ? args[1] : "ordens.bin");
            case "in-stdin" -> inputFromStdin();
            case "out-tcp" -> outputToTcp(args.length > 1 ? args[1] : "localhost", args.length > 2 ? Integer.parseInt(args[2]) : 9090);
            default -> printHelp();
        }
    }

    private static OrdemInvestimento[] amostra() {
        return new OrdemInvestimento[]{
            new OrdemInvestimento("O-1", "PETR4", "COMPRA", 100, 38.45),
            new OrdemInvestimento("O-2", "VALE3", "VENDA", 50, 67.90)
        };
    }

    private static void outputToStdout() throws IOException {
        try (OrdemInvestimentoOutputStream out = new OrdemInvestimentoOutputStream(amostra(), 2, new FilterOutputStream(System.out) {
            @Override
            public void close() throws IOException {
                flush();
            }
        })) {
            out.writeOrdens();
        }
        System.err.println("Bytes binarios enviados para System.out");
    }

    private static void outputToFile(String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path);
             OrdemInvestimentoOutputStream out = new OrdemInvestimentoOutputStream(amostra(), 2, fos)) {
            out.writeOrdens();
        }
        System.out.println("Arquivo gerado em: " + path);
    }

    private static void inputFromFile(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
             OrdemInvestimentoInputStream in = new OrdemInvestimentoInputStream(fis)) {
            OrdemInvestimento[] ordens = in.readOrdens();
            System.out.println(Arrays.toString(ordens));
        }
    }

    private static void inputFromStdin() throws IOException {
        try (OrdemInvestimentoInputStream in = new OrdemInvestimentoInputStream(new FilterInputStream(System.in) {
            @Override
            public void close() {
                // Keep stdin open for the JVM process.
            }
        })) {
            OrdemInvestimento[] ordens = in.readOrdens();
            System.out.println(Arrays.toString(ordens));
        }
    }

    private static void outputToTcp(String host, int port) throws IOException {
        try (Socket socket = new Socket(host, port);
             OrdemInvestimentoOutputStream out = new OrdemInvestimentoOutputStream(amostra(), 2, socket.getOutputStream())) {
            out.writeOrdens();
            System.out.println("Ordens enviadas para " + host + ":" + port);
        }
    }

    private static void printHelp() {
        System.out.println("Uso:");
        System.out.println("  out-stdout");
        System.out.println("  out-file [arquivo]");
        System.out.println("  in-file [arquivo]");
        System.out.println("  in-stdin");
        System.out.println("  out-tcp [host] [porta]");
    }
}
