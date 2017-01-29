package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.model.HexData;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
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

        if (status != 0) {
            clean(tmp);
            return new Result(false);
        }

        Stream<Path> bins = Files.list(tmp).filter((f) -> f.getFileName().toString().endsWith(".bin"));

        List<CompiledContract> contracts = bins.map((path -> {
            String name = path.getFileName().toString();
            name = name.substring(0, name.length() - ".bin".length());
            return name;
        })).map((name) -> {
            Path bin = tmp.resolve(name + ".bin");
            HexData binData = null;
            try {
                if (bin.toFile().length() > 0) {
                    List<String> data = Files.readAllLines(bin);
                    binData = HexData.from("0x" + data.get(0));
                }
            } catch (IOException e) {
                stderrLines.add(e.getMessage());
            }

            Path abi = tmp.resolve(name + ".abi");
            String json = null;
            try {
                json = String.join("", Files.readAllLines(abi));
            } catch (IOException e) {
                stderrLines.add(e.getMessage());
            }

            return new CompiledContract(name, binData, json);
        }).collect(Collectors.toList());

        return new Result(true).add(contracts);
    }

    private void clean(Path tmp) throws IOException {
        Files.walk(tmp, FileVisitOption.FOLLOW_LINKS)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    public static class Result {
        private boolean success;
        private List<CompiledContract> contracts;

        public Result(boolean success) {
            this.success = success;
            contracts = new ArrayList<>();
        }

        public List<String> getNames() {
            return contracts.stream()
                .map(CompiledContract::getName)
                .collect(Collectors.toList());
        }

        public CompiledContract getContract(String name) {
            for(CompiledContract c: contracts) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
            return null;
        }

        public List<CompiledContract> getContracts() {
            return contracts;
        }

        public Result add(CompiledContract contract) {
            contracts.add(contract);
            return this;
        }
        public Result add(List<CompiledContract> contracts) {
            this.contracts.addAll(contracts);
            return this;
        }

        public int getCount() {
            return contracts.size();
        }
    }

    public static class CompiledContract {
        private final String name;
        private HexData compiled;
        private String abi;

        public CompiledContract(String name) {
            this.name = name;
        }

        public CompiledContract(String name, HexData compiled, String abi) {
            this(name);
            this.compiled = compiled;
            this.abi = abi;
        }

        public void setCompiled(HexData compiled) {
            this.compiled = compiled;
        }

        public void setAbi(String abi) {
            this.abi = abi;
        }

        public HexData getCompiled() {
            return compiled;
        }

        public String getAbi() {
            return abi;
        }

        public String getName() {
            return name;
        }
    }

}
