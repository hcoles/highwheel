package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.modules.model.Module;

import java.util.Collection;

public class ModuleDependenciesGraphBuildingVisitor implements AccessVisitor {

    public ModuleDependenciesGraphBuildingVisitor(
            final Collection<Module> modules,
            final ModuleGraph graph) {

    }

    @Override
    public void apply(AccessPoint source, AccessPoint dest, AccessType type) {

    }

    @Override
    public void newNode(ElementName clazz) {

    }

    @Override
    public void newAccessPoint(AccessPoint ap) {

    }

    @Override
    public void newEntryPoint(ElementName clazz) {

    }
}
