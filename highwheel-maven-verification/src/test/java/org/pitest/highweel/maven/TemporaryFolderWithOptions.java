package org.pitest.highweel.maven;

import org.junit.rules.TemporaryFolder;

/**
 * Extends standard TemporaryFolder rule so that cleanup may be
 * temporarily disabled
 */
public class TemporaryFolderWithOptions extends TemporaryFolder {
  
  private final boolean clean;
  
  TemporaryFolderWithOptions() {
    this(true);
  }
  
  public TemporaryFolderWithOptions(boolean clean) {
    this.clean = clean;
  }
  
  @Override
  protected void after() {
    if(clean) {
      delete();
    }
  }

}
