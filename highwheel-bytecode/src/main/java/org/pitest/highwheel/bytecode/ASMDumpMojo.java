package org.pitest.highwheel.bytecode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;

/**
 * 
 * @goal asmDump
 * 
 * @requiresDependencyResolution compile
 * 
 */
public class ASMDumpMojo extends BaseMojo {

  @Override
  protected void analyse(final ClasspathRoot cpr, final Filter filter)
      throws MojoExecutionException {
    try {
      final File dir = this.makeReportDirectory("bytecode");

      for (final ElementName element : cpr.classNames()) {
        File outDir = new File(dir + File.separator
            + element.getParent().asJavaName().replace(".", File.separator));
        outDir.mkdirs();
        final FileOutputStream fos = new FileOutputStream(outDir + File.separator + element.getNameWithoutPackage().asJavaName() + ".bytecode");
        try {
          final ClassReader reader = new ClassReader(cpr.getData(element));
          reader.accept(new TraceClassVisitor(null, new Textifier(),
              new PrintWriter(fos)), 0);
        } finally {
          fos.close();
        }
      }

    } catch (final IOException ex) {
      throw new MojoExecutionException("Error while scanning codebase", ex);
    }
  }

}
