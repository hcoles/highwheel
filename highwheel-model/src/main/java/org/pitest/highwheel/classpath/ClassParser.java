package org.pitest.highwheel.classpath;

import java.io.IOException;

public interface ClassParser {

   public void parse(ClasspathRoot cp, final AccessVisitor v) throws IOException;
	
}
