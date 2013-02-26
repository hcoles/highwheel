package org.pitest.highwheel.report.html;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.pitest.highwheel.report.StreamFactory;

public class BaseHtmlWriter {

  protected final StreamFactory streams;

  protected BaseHtmlWriter(final StreamFactory streams) {
    this.streams = streams;
  }

  protected final void writeHeader(final String stream) {
    write(stream, "<!DOCTYPE HTML><html>");
    write(stream, "<head>");
    write(stream, "<link rel=\"stylesheet\" href=\"style.css\">");
    write(stream, "</head>");
    write(stream, "<body>");
    write(stream, "<article>");
  }

  protected final void writeFooter(final String stream) {
    write(stream, "</article>");
    write(stream, "</body>");
  }

  protected final void write(final String stream, final String value) {
    try {
      final Writer w = new OutputStreamWriter(this.streams.getStream(stream));
      w.write(value);
      w.flush();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

}
