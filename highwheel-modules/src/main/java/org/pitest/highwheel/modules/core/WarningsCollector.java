package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.model.Module;

import java.util.Collection;

public interface WarningsCollector {
    void constructionWarning(Module m);
    void accessPointWarning(ElementName elementName, Collection<Module> matchingModules);
}
