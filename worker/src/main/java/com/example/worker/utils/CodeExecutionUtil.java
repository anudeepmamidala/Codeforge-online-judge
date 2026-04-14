package com.example.worker.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CodeExecutionUtil {

    public record ExecutionResult(
            boolean passed,
            String stdout,
            String stderr,
            String verdict
    ) {}

    public static ExecutionResult execute(
            String code, String input, String expectedOutput, String language, String validationType
    ) {
        return switch (language.toUpperCase()) {
            case "PYTHON" -> runPython(code, input, expectedOutput, validationType);
            case "JAVA"   -> runJava(code, input, expectedOutput, validationType);
            case "CPP"    -> runCpp(code, input, expectedOutput, validationType);
            default -> new ExecutionResult(false, "", "Unsupported language", "ERROR");
        };
    }

    private static ExecutionResult runPython(String code, String input, String expectedOutput, String validationType) {
        String jobId = UUID.randomUUID().toString();
        String folder = "job_" + jobId;
        return runContainer(code, input, expectedOutput, "python:3.9", 
                new String[]{"python3", "Main.py"}, folder, "Main.py", validationType);
    }

    private static ExecutionResult runJava(String code, String input, String expectedOutput, String validationType) {
        String jobId = UUID.randomUUID().toString();
        String folder = "job_" + jobId;
        String command = "javac Main.java && java Main";
        return runContainer(code, input, expectedOutput, "eclipse-temurin:17", 
                new String[]{"sh", "-c", command}, folder, "Main.java", validationType);
    }

    private static ExecutionResult runCpp(String code, String input, String expectedOutput, String validationType) {
        String jobId = UUID.randomUUID().toString();
        String folder = "job_" + jobId;
        String command = "g++ main.cpp -o main.out && ./main.out";
        return runContainer(code, input, expectedOutput, "gcc:latest", 
                new String[]{"sh", "-c", command}, folder, "main.cpp", validationType);
    }

    private static ExecutionResult runContainer(
            String code, String input, String expectedOutput, String image, 
            String[] command, String folderName, String fileName, String validationType
    ) {
        Path baseDir = Paths.get("/shared");
        Path jobFolder = baseDir.resolve(folderName);
        Path filePath = jobFolder.resolve(fileName);

        try {
            Files.createDirectories(jobFolder);
            Files.writeString(filePath, code);

            String[] dockerCmd = buildDockerCommand(image, command, folderName);

            ProcessBuilder pb = new ProcessBuilder(dockerCmd);
            Process process = pb.start();

            new Thread(() -> {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    if (input != null && !input.isBlank()) {
                        writer.write(input.replace("\\n", "\n"));
                        writer.flush();
                    }
                } catch (IOException ignored) {}
            }).start();

            StringBuilder stdoutBuf = new StringBuilder();
            StringBuilder stderrBuf = new StringBuilder();

            Thread out = new Thread(() -> { try { stdoutBuf.append(read(process.getInputStream())); } catch (Exception ignored) {} });
            Thread err = new Thread(() -> { try { stderrBuf.append(read(process.getErrorStream())); } catch (Exception ignored) {} });
            
            out.start(); err.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS); 
            out.join(); err.join();

            String stdout = stdoutBuf.toString();
            String stderr = stderrBuf.toString();

            if (!finished) {
                process.destroyForcibly();
                delete(jobFolder);
                return new ExecutionResult(false, "", "Time Limit Exceeded", "TIMEOUT");
            }

            if (process.exitValue() != 0) {
                delete(jobFolder);
                return new ExecutionResult(false, stdout, stderr, "ERROR");
            }

            String actual = normalize(stdout);
            String expected = normalize(expectedOutput);
            boolean passed = validate(actual, expected, validationType);

            delete(jobFolder);
            return new ExecutionResult(passed, actual, stderr, passed ? "PASSED" : "FAILED");

        } catch (Exception e) {
            delete(jobFolder);
            return new ExecutionResult(false, "", e.getMessage(), "ERROR");
        }
    }

    private static String[] buildDockerCommand(String image, String[] command, String folderName) {
        String hostPath = System.getenv("HOST_SHARED_PATH");
        if (hostPath == null) hostPath = "/shared";

        String specificJobHostPath = hostPath + "/" + folderName;

        String[] base = new String[]{
                "docker", "run", "--rm", "-i",
                "--memory=128m", "--cpus=0.5", "--pids-limit=64", "--network=none",
                "-v", specificJobHostPath + ":/app",
                "-w", "/app", 
                image
        };

        String[] full = new String[base.length + command.length];
        System.arraycopy(base, 0, full, 0, base.length);
        System.arraycopy(command, 0, full, base.length, command.length);
        return full;
    }

    // ... (validate, compareTokens, read, delete, normalize methods remain the same)
    private static boolean validate(String actual, String expected, String type) {
        if (type == null) type = "EXACT_MATCH";
        return type.equals("TOKEN_MATCH") ? compareTokens(actual, expected) : actual.equals(expected);
    }

    private static boolean compareTokens(String a, String b) {
        String[] t1 = a.trim().split("\\s+");
        String[] t2 = b.trim().split("\\s+");
        if (t1.length != t2.length) return false;
        for (int i = 0; i < t1.length; i++) if (!t1[i].equals(t2[i])) return false;
        return true;
    }

    private static String read(InputStream is) throws IOException { return new String(is.readAllBytes()); }

    private static void delete(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return Arrays.stream(s.trim().replaceAll("\\r\\n|\\r", "\n").split("\n"))
                .map(String::trim).collect(Collectors.joining("\n"));
    }
}
