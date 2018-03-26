package org.pitest.highwheel.modules.cli;

import org.pitest.highwheel.modules.AnalyserFacade;

import java.util.List;

import static org.pitest.highwheel.util.StringUtil.join;

public class Main {

    private static class SystemPrinter implements AnalyserFacade.Printer {

        @Override
        public void info(String msg) {
            System.out.println(" - " + msg);
        }
    }

    private static class SystemPathEventSink implements AnalyserFacade.EventSink.PathEventSink {

      @Override
      public void ignoredPaths(List<String> ignored) {
        System.out.println(" - Ignored: " + join(", ", ignored));
      }

      @Override
      public void directories(List<String> directories) {
        System.out.println(" - Directories: " + join(", ", directories));
      }

      @Override
      public void jars(List<String> jars) {
        System.out.println(" - Jars: " + join(", ", jars));
      }
    }

    private static class SystemMeasureSink implements AnalyserFacade.EventSink.MeasureEventSink {

      @Override
      public void fanInOutMeasure(String module, int fanIn, int fanOut) {
        System.out.println(String.format("  - %20s --> fanIn: %5d, fanOut: %5d", module, fanIn, fanOut));
      }
    }

    private static class SystemStrictAnalysisSink implements AnalyserFacade.EventSink.StrictAnalysisEventSink {

      @Override
      public void dependenciesCorrect() {
        System.out.println(" - No dependency violation detected");
      }

      @Override
      public void directDependenciesCorrect() {
        System.out.println(" - No direct dependency violation detected");
      }

      @Override
      public void dependencyViolationsPresent() {
        System.err.println(" - The following dependencies violate the specification:");
      }

      @Override
      public void dependencyViolation(String sourceModule, String destModule, List<String> expectedPath,
          List<String> actualPath) {
        System.err.println(String.format("  %s -> %s. Expected path: %s, Actual path: %s",
                sourceModule,
                destModule,
                printGraphPath(expectedPath),
                printGraphPath(actualPath)
            ));
      }

      @Override
      public void noDirectDependenciesViolationPresent() {
        System.err.println(" - The following direct dependencies violate the specification:");
      }

      @Override
      public void noDirectDependencyViolation(String sourceModule, String destModule) {
        System.err.println(String.format("  %s -> %s",
            sourceModule,
            destModule
        ));
      }
    }

    private static class SystemLooseAnalysisEventSink implements AnalyserFacade.EventSink.LooseAnalysisEventSink {

      @Override
      public void allDependenciesPresent() {
        System.out.println(" - All dependencies specified exist");
      }

      @Override
      public void noUndesiredDependencies() {
        System.out.println(" - No dependency violation detected");
      }

      @Override
      public void absentDependencyViolationsPresent() {
        System.err.println(" - The following dependencies do not exist:");
      }

      @Override
      public void absentDependencyViolation(String sourceModule, String destModule) {
        System.err.println(String.format("  %s -> %s",
            sourceModule,
            destModule
        ));
      }

      @Override
      public void undesiredDependencyViolationsPresent() {
        System.err.println(" - The following dependencies violate the specification:");
      }

      @Override
      public void undesiredDependencyViolation(String sourceModule, String destModule, List<String> path) {
        System.err.println(String.format("  %s -/-> %s. Actual path: %s",
            sourceModule,
            destModule,
            printGraphPath(path)
        ));
      }
    }

    private static String printGraphPath(List<String> pathComponents) {
      if(pathComponents.isEmpty()) {
        return "(empty)";
      } else {
        return join(" -> ", pathComponents);
      }
    }

    public static void main(String[] argv) {
        try {
            final CmdParser cmdParser = new CmdParser(argv);
            final AnalyserFacade.Printer printer = new SystemPrinter();
            final AnalyserFacade facade = new AnalyserFacade(printer, new SystemPathEventSink(), new SystemMeasureSink(), new SystemStrictAnalysisSink(), new SystemLooseAnalysisEventSink());
            facade.runAnalysis(cmdParser.argList,cmdParser.specificationFile,cmdParser.mode);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
