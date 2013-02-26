package org.pitest.highwheel.oracle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.util.GlobToRegex;

/**
 * Oracle that matches classes against an allowed/disallowed list expressed as
 * globs
 */
public class SimpleFlatFileOracleParser {

  private final BufferedReader r;

  public SimpleFlatFileOracleParser(final InputStream is) {
    this.r = new BufferedReader(new InputStreamReader(is));
  }

  public DependencyOracle parse() throws IOException {
    final List<DependencyOracle> dos = new ArrayList<DependencyOracle>();
    String line = this.r.readLine();
    while (line != null) {
      final String[] parts = line.split("->");
      dos.add(makeGlobOracle(parts[0].trim(), parts[1].trim()));
      line = this.r.readLine();
    }
    return new CompoundOracle(dos);
  }

  private static DependencyOracle makeGlobOracle(final String lhs,
      final String rhs) {
    final Pattern fromPattern = Pattern.compile(GlobToRegex
        .convertGlobToRegex(lhs));
    final String glob = rhs.split(":")[0].trim();
    final Pattern toPattern = Pattern.compile(GlobToRegex
        .convertGlobToRegex(glob));
    final DependendencyStatus status = DependendencyStatus.valueOf(rhs
        .split(":")[1].trim());
    return new DependencyOracle() {

      public DependendencyStatus assess(final Access a) {

        Matcher m = fromPattern.matcher(a.getSource().getElementName()
            .asJavaName());
        if (m.matches()) {
          m = toPattern.matcher(a.getDest().getElementName().asJavaName());
          if (m.matches()) {
            return status;
          }
        }

        return DependendencyStatus.UNKNOWN;
      }

    };
  }

}
