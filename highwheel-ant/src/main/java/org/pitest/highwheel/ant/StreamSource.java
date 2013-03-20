package org.pitest.highwheel.ant;

import java.io.File;

import org.pitest.highwheel.report.FileStreamFactory;

class StreamSource {
  FileStreamFactory get(final File dir) {
    return new FileStreamFactory(dir);
  }

}
