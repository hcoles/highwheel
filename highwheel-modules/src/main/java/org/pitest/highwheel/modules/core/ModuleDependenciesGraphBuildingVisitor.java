package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.modules.model.Module;

import java.util.*;

public class ModuleDependenciesGraphBuildingVisitor implements AccessVisitor {

    private static class NoOpWarningsCollector implements WarningsCollector {
        @Override
        public void constructionWarning(Module m) {}

        @Override
        public void accessPointWarning(ElementName elementName, Collection<Module> message) {}
    }

    private final Collection<Module> modules;
    private final ModuleGraph graph;
    private final WarningsCollector warningsCollector;
    private final Module other;

    public ModuleDependenciesGraphBuildingVisitor(
            final Collection<Module> modules,
            final ModuleGraph graph,
            final Module other,
            final WarningsCollector warningsCollector) {
        this. modules = modules;
        this.graph = graph;
        this.warningsCollector = warningsCollector;
        this.other = other;
        addModulesToGraph();
    }

    private void addModulesToGraph() {
      graph.addModule(other);
        final Set<String> processedModuleNames = new HashSet<String>(modules.size());
        for(Module module: modules) {
            graph.addModule(module);
            if(processedModuleNames.contains(module.name)) {
                warningsCollector.constructionWarning(module);
            }
            processedModuleNames.add(module.name);
        }
    }

    public ModuleDependenciesGraphBuildingVisitor(final Collection<Module> modules, final ModuleGraph graph, final Module other) {
        this(modules,graph,other, new NoOpWarningsCollector());
    }

    @Override
    public void apply(AccessPoint source, AccessPoint dest, AccessType type) {
        final List<Module> modulesMatchingSource = getMatchingModules(source.getElementName());
        final List<Module> moduleMatchingDest = getMatchingModules(dest.getElementName());

        for(Module sourceModule : modulesMatchingSource) {
            for(Module destModule: moduleMatchingDest) {
                if(!sourceModule.equals(destModule))
                    graph.addDependency(sourceModule,destModule);
            }
        }

        if(modulesMatchingSource.isEmpty() && ! moduleMatchingDest.isEmpty()) {
          for(Module destModule: moduleMatchingDest) {
            graph.addDependency(other,destModule);
          }
        }

        if(!modulesMatchingSource.isEmpty() && moduleMatchingDest.isEmpty()) {
          for(Module sourceModule: modulesMatchingSource) {
            graph.addDependency(sourceModule,other);
          }
        }
    }

    private List<Module> getMatchingModules(ElementName name) {
        final List<Module> modulesMatchingName = new ArrayList<Module>(modules.size());
        for(Module module : modules) {
            if(module.contains(name)){
                modulesMatchingName.add(module);
            }
        }
        if(modulesMatchingName.size() > 1) {
            warningsCollector.accessPointWarning(name,modulesMatchingName);
        }
        return modulesMatchingName;
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
