package org.pitest.highwheel.report.html;

import java.util.Arrays;
import java.util.Collection;

import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.StreamFactory;

public class HtmlCycleWriter extends CompoundCycleVisitor {

  public HtmlCycleWriter(final DependencyOracle dependencyScorer,
      final StreamFactory streams) {
    super(makeChildren(dependencyScorer, streams));
  }

  private static Collection<CycleReporter> makeChildren(
      final DependencyOracle dependencyScorer, final StreamFactory streams) {
    return Arrays.<CycleReporter> asList(new IndexWriter(dependencyScorer,
        streams), new CycleWriter(dependencyScorer, streams),
        new ClassesWriter(streams), new PackagesWriter(streams),
        new ResourceWriter(streams));
  }

}
