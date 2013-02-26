package org.pitest.highwheel.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


public class FileStreamFactory implements StreamFactory {

  private final File                      directory;

  private final Map<String, OutputStream> streams = new HashMap<String, OutputStream>();

  public FileStreamFactory(final File directory) {
    this.directory = directory;
  }

  public OutputStream getStream(final String name) {
    OutputStream os = this.streams.get(name);
    if (os != null) {
      return os;
    }
    try {
      os = new FileOutputStream(new File(this.directory, name));
      this.streams.put(name, os);
      return os;
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void close() {
    for (final OutputStream each : this.streams.values()) {
      try {
        each.close();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }

  }

}
