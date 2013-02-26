package org.pitest.highwheel.report;

import java.io.OutputStream;

public interface StreamFactory {

  public OutputStream getStream(String name);

  public void close();
}
