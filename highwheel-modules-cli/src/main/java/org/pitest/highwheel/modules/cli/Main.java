package org.pitest.highwheel.modules.cli;

import org.pitest.highwheel.modules.AnalyserFacade;

public class Main {

    private static class SystemPrinter implements AnalyserFacade.Printer {
        private final String INDENTATION = "  ";
        @Override
        public void info(String msg, int indentation) {
            System.out.println(indent(indentation) + " - " + msg);
        }

        private String indent(int num) {
            final StringBuilder builder = new StringBuilder("");
            for(int i =0; i < num; ++i) {
                builder.append(INDENTATION);
            }
            return builder.toString();
        }

        @Override
        public void info(String msg) {
            System.out.println(" - " + msg);
        }

        @Override
        public void error(String msg, int indentation) {
            System.err.println(indent(indentation) + " - " + msg);
        }

        @Override
        public void error(String msg) {
            System.err.println(" - " + msg);
        }

      @Override
      public void warning(String msg) {
        this.info(msg);
      }
    }

    public static void main(String[] argv) {
        try {
            final CmdParser cmdParser = new CmdParser(argv);
            final AnalyserFacade.Printer printer = new SystemPrinter();
            final AnalyserFacade facade = new AnalyserFacade(printer);
            facade.runAnalysis(cmdParser.argList,cmdParser.specificationFile,cmdParser.mode);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
