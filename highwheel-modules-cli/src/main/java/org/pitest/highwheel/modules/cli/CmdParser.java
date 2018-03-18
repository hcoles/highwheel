package org.pitest.highwheel.modules.cli;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;

public class CmdParser {
    private static String STRICT = "strict";
    private static String LOOSE = "loose";

    private final Options options = new Options();
    private final Option spec = Option.builder("s")
            .longOpt("specification")
            .optionalArg(false)
            .hasArg(true)
            .desc("Path to the specification file").build();
    private final Option modeOpt = Option.builder("m")
            .longOpt("modeOpt")
            .optionalArg(false)
            .hasArg(true)
            .desc("Mode of analysis. Can be 'strict' or 'loose'").build();

    public final ExecutionMode mode;
    public final File specificationFile;
    public final List<String> argList;

    public CmdParser(String[] argv) {
        options.addOption(spec).addOption(modeOpt);

        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, argv);
        } catch(ParseException e) {
            throw new CliException("Error while parsing the command line arguments: "+ e.getMessage());
        }
        String specificationPath = cmd.getOptionValue("specification","spec.hwm");
        String operationMode = cmd.getOptionValue("modeOpt",STRICT);
        if(! operationMode.equals(STRICT) && !operationMode.equals(LOOSE)) {
            throw new CliException("Unrecognised value for modeOpt: " + operationMode + ". Select 'strict' or 'loose'");
        }

        if(operationMode.equals(STRICT)) {
            this.mode = ExecutionMode.STRICT;
        } else {
            this.mode = ExecutionMode.LOOSE;
        }

        final File f = new File(specificationPath);
        if(!f.exists() || f.isDirectory() || !f.canRead()) {
            throw new CliException(String.format("Cannot read from specification file '%s'.", specificationPath));
        }
        specificationFile = f;
        argList = cmd.getArgList();
    }
}
