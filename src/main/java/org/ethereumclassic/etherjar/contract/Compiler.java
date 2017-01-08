package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.model.HexData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Igor Artamonov
 */
public class Compiler {

    private File solc;

    public Compiler(String solc) {
        this.solc = new File(solc);
    }

    public Result compile(File source, boolean optimize) throws IOException, InterruptedException {
        return compile(new FileInputStream(source), optimize);
    }

    public Result compile(String source, boolean optimize) throws IOException, InterruptedException {
        return compile(new ByteArrayInputStream(source.getBytes("UTF-8")), optimize);
    }

    //TODO support multiple sources
    public Result compile(InputStream source, boolean optimize) throws IOException, InterruptedException {
        Path tmp = Files.createTempDirectory("etherjar-compile");
        Path contractSource = Files.createTempFile(tmp, "contract", ".sol");
        Files.copy(source, contractSource, StandardCopyOption.REPLACE_EXISTING);

        List<String> args = new ArrayList<>();
        args.add(solc.getAbsolutePath());
        if (optimize) {
            args.add("--optimize");
        }
        args.add("--abi");
        args.add("--bin");
        args.add("--output-dir"); args.add("./");
        args.add("./" + contractSource.getFileName().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(args)
            .directory(tmp.toFile());
        Process process = processBuilder.start();

        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        List<String> stdoutLines = new ArrayList<>();
        List<String> stderrLines = new ArrayList<>();
        while ((line = stdout.readLine()) != null) {
            stdoutLines.add(line);
        }
        while ((line = stderr.readLine()) != null) {
            stderrLines.add(line);
        }

        int status = process.waitFor();


        Stream<Path> bins = Files.list(tmp).filter((f) -> f.getFileName().toString().endsWith(".bin"));
        Stream<Path> abis = Files.list(tmp).filter((f) -> f.getFileName().toString().endsWith(".abi"));

        //TODO delete temp files after processing

        if (status != 0) {
            return new Result(false);
        }

        //TODO extract contract name

        Optional<Path> bin = bins.findFirst();
        HexData binData = null;
        if (bin.isPresent()) {
            List<String> data = Files.readAllLines(bin.get());
            binData = HexData.from("0x" + data.get(0));
        }

        Optional<Path> abi = abis.findFirst();
        String json = null;
        if (abi.isPresent()) {
            BufferedReader rdr = Files.newBufferedReader(abi.get());
            StringBuffer buf = new StringBuffer();
            while ((line = rdr.readLine()) != null) {
                buf.append(line);
            }
            json = buf.toString();
        }

        return new Result(binData, json, true);
    }

    public static class Result {
        private HexData compiled;
        private String abi;
        private boolean success;

        public Result(boolean success) {
            this.success = success;
        }

        public Result(HexData compiled, String abi, boolean success) {
            this.compiled = compiled;
            this.abi = abi;
            this.success = success;
        }

        public HexData getCompiled() {
            return compiled;
        }

        public String getAbi() {
            return abi;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
