package org.pitest.highwheel.losttests;

import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.report.FileStreamFactory;
import org.pitest.highwheel.report.html.BaseHtmlWriter;

public class LostTestHTMLVisitor extends BaseHtmlWriter implements
    LostTestVisitor {

  public final static String FILENAME = "lost_tests.html";

  public LostTestHTMLVisitor(final FileStreamFactory fsf) {
    super(fsf);
  }

  public void start() {
    writeHeader(FILENAME);
    write(FILENAME, "<section class ='deps'>");
    write(FILENAME, "<h1>Tests that may be in wrong package</h1>");
    write(
        FILENAME,
        "<table id=\"sorttable\" class=\"tablesorter\"><thead><tr><th>Test</th><th>Detected Testee</th><th>Suggested package</th></tr></thead>");
    write(FILENAME, "<tbody>");
  }

  public void visitLostTest(final ElementName test, final ElementName testee) {
    write(FILENAME, "<tr><td>" + test.asJavaName() + "</td><td>" 
      + testee.asJavaName() + "</td><td>" + testee.getParent().asJavaName()
        + "</td></tr>");
  }

  public void end() {
    write(FILENAME, "</tbody>");
    write(FILENAME, "</table>");
    write(FILENAME, "</section");
    writeFooter(FILENAME);
  }

}
