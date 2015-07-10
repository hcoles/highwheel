package org.pitest.highweel.maven;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pitest.highwheel.Highwheel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HighWheelIT {

  private final static String  VERSION    = getVersion();
  
  private final static Logger LOG = Logger.getAnonymousLogger();

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolderWithOptions(false);

  private Verifier       verifier;
 
  @Test
  public void shouldProduceAnIndexFile() throws Exception {
    File testDir = analyse("/minimal-test");
    Document actual = readIndex(testDir);
    assertThat(actual.getElementsByTagName("body").getLength()).isEqualTo(1);
  }
  
  private File analyse(String project) throws Exception {
    File testDir = prepare(project);
    runHighwheel();
    LOG.info("Analysed " + project + " at " + testDir);
    return testDir;
  }

  private void runHighwheel() throws VerificationException {
    verifier.executeGoal("compile");  
    verifier.executeGoal("org.pitest:highwheel-maven:" + VERSION + ":analyse");  
  }

  private File prepare(String testPath) throws IOException,
      VerificationException {
    String path = ResourceExtractor.extractResourcePath(getClass(), testPath,
        testFolder.getRoot(), true).getAbsolutePath();
    
    verifier = new Verifier(path);
    verifier.setAutoclean(false);
    verifier.setDebug(true);
    return new File(testFolder.getRoot().getAbsolutePath() + testPath);
  }
  
  private Document readIndex(File testDir) throws Exception {
    File indexFile = new File(testDir.getAbsoluteFile() + File.separator
        + "target" + File.separator + "highwheel" + File.separator
        + "index.html");
    return parseHtml(indexFile);
  }

  private static String getVersion() {
    String path = "/version.prop";
    InputStream stream = Highwheel.class.getResourceAsStream(path);
    Properties props = new Properties();
    try {
      props.load(stream);
      stream.close();
      return (String) props.get("version");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Document parseHtml(File file) throws SAXException, IOException {
    final HtmlDocumentBuilder html = new HtmlDocumentBuilder(
        XmlViolationPolicy.FATAL);
    return html.parse(file);
  }
  

}
