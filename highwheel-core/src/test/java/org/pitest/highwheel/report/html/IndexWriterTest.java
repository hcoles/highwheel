package org.pitest.highwheel.report.html;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.cycles.CodeGraphs;
import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.StreamFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class IndexWriterTest {

  private IndexWriter           testee;

  @Mock
  private DependencyOracle      scorer;

  @Mock
  private StreamFactory         streams;

  private ByteArrayOutputStream os;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    this.os = new ByteArrayOutputStream();
    when(this.streams.getStream(IndexWriter.INDEX)).thenReturn(this.os);

    this.testee = new IndexWriter(this.scorer, this.streams);
  }

  @Test
  public void shouldGenerateValidDocument() throws Exception {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    final Document d = parseOutput();
    assertThat(d.getElementsByTagName("html").getLength()).isEqualTo(1);
    assertThat(d.getElementsByTagName("body").getLength()).isEqualTo(1);
  }

  @Test
  public void shouldGenerateAHeaderSection() throws Exception {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    final Document d = parseOutput();
    final NodeList header = d.getElementsByTagName("header");

    assertThat(header.getLength()).isEqualTo(1);
    assertThat(header.item(0).getFirstChild().getNodeName()).isEqualTo("h1");
  }

  @Test
  public void shouldLinkToClassesReport() throws SAXException, IOException {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    final Document d = parseOutput();
    final NodeList links = d.getElementsByTagName("a");
    assertThat(
        links.item(0).getAttributes().getNamedItem("href").getTextContent())
        .isEqualTo("classes.html");
  }

  @Test
  public void shouldLinkToPackagesReport() throws SAXException, IOException {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    final Document d = parseOutput();
    final NodeList links = d.getElementsByTagName("a");
    assertThat(
        links.item(1).getAttributes().getNamedItem("href").getTextContent())
        .isEqualTo("packages.html");
  }

  @Test
  public void shouldLinkToPackageSccs() throws SAXException, IOException {
    final DirectedSparseGraph<ElementName, Dependency> scc = smallCycle();
    this.testee.visitPackageScc(scc);

    assertFirstLinkIs("package_tangle_0.html");
  }

  @Test
  public void shouldLinkToClassccs() throws SAXException, IOException {
    final DirectedSparseGraph<ElementName, Dependency> scc = smallCycle();
    this.testee.visitClassScc(scc);

    assertFirstLinkIs("class_tangle_0.html");
  }

  private void assertFirstLinkIs(String value) throws SAXException, IOException {
    final Document d = parseOutput();
    final NodeList links = d.getElementsByTagName("a");
    assertThat(
        links.item(0).getAttributes().getNamedItem("href").getTextContent())
        .isEqualTo(value);
  }
  
  private DirectedSparseGraph<ElementName, Dependency> smallCycle() {
    final DirectedSparseGraph<ElementName, Dependency> scc = new DirectedSparseGraph<ElementName, Dependency>();
    final Dependency dep = new Dependency();
    scc.addEdge(dep, ElementName.fromString("foo"),
        ElementName.fromString("bar"));
    return scc;
  }

  private Document parseOutput() throws SAXException, IOException {
    final HtmlDocumentBuilder html = new HtmlDocumentBuilder(
        XmlViolationPolicy.FATAL);
    final ByteArrayInputStream bis = new ByteArrayInputStream(
        this.os.toByteArray());
    return html.parse(bis);
  }

  private CodeStats emptyCodeStats() {
    final CodeGraphs g = new CodeGraphs(
        new DirectedSparseGraph<ElementName, Dependency>());
    return new CodeStats(g);
  }

}
