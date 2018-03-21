package org.pitest.highwheel.modules.cli;

import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.bytecodeparser.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.DirectoryClassPathRoot;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.AnalyserFacade;
import org.pitest.highwheel.modules.AnalyserModel;
import org.pitest.highwheel.modules.ModuleAnalyser;
import org.pitest.highwheel.modules.model.Definition;
import org.pitest.highwheel.modules.specification.Compiler;
import org.pitest.highwheel.modules.specification.SyntaxTree;
import org.pitest.highwheel.modules.specification.parsers.DefinitionParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.pitest.highwheel.util.StringUtil.join;

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
