package org.pitest.highwheel.report.html;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.report.StreamFactory;

public class ResourceWriterTest {
  
  private ResourceWriter           testee;

  @Mock
  private StreamFactory         streams;

  @Mock
  private CodeStats stats;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.streams.getStream(any(String.class))).thenReturn(new ByteArrayOutputStream());
    this.testee = new ResourceWriter(this.streams);
  }

  @Test
  public void shouldCopyCssToOutputLocation() {
    testee.start(stats);
    verify(this.streams).getStream("style.css");
  }
  
  @Test
  public void shouldCopyTableSorterJSLibrariesToOutputLocation() {
    testee.start(stats);
    verify(this.streams).getStream("jquery-latest.js");
    verify(this.streams).getStream("jquery.tablesorter.min.js");
  }

  @Test
  public void shouldCopyTableSortingImagesToOutputLocation() {
    testee.start(stats);
    verify(this.streams).getStream("asc.gif");
    verify(this.streams).getStream("bg.gif");
    verify(this.streams).getStream("desc.gif");
  }
}
