package org.pitest.highwheel.modules.core;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.junit.Test;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.externaladapters.JungModuleGraph;
import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleDependency;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;

public class ModuleDependenciesGraphBuildingVisitorTest {

    private final Module SUPER_MODULE = Module.make("SuperModule", "org.example.*").get();
    private final Module CORE = Module.make("Core", "org.example.core.*").get();
    private final Module IO = Module.make("IO", "org.example.io.*").get();
    private final Module COMMONS = Module.make("Commons", "org.example.commons.*").get();
    private final Module ENDPOINTS = Module.make("Endpoints", "org.example.endpoints.*").get();
    private final Module MAIN = Module.make("Main","org.example.Main").get();
    private final Module OTHER = Module.make("Other","").get();

    private final List<Module> modules = Arrays.asList(CORE,IO,COMMONS,ENDPOINTS,MAIN);

    private static class Pair<T1,T2> {
        public final T1 first;
        public final T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    private static <T1,T2> Pair<T1,T2> makePair(T1 first, T2 second) {
        return new Pair<T1,T2>(first,second);
    }

    private final List<Module> constructionWarnings = new ArrayList<Module>(5);
    private final List<Pair<ElementName,Collection<Module>>> visitWarnings = new ArrayList<Pair<ElementName, Collection<Module>>>(5);
    private class AddToListWarnings implements WarningsCollector {

        @Override
        public void constructionWarning(final Module m) {
            constructionWarnings.add(m);
        }

        @Override
        public void accessPointWarning(ElementName ap, Collection<Module> matchingModules) {
            visitWarnings.add(makePair(ap,matchingModules));
        }
    }
    private final DirectedSparseGraph<Module,ModuleDependency> graph = new DirectedSparseGraph<Module, ModuleDependency>();
    private final JungModuleGraph moduleGraph = new JungModuleGraph(graph);
    private final WarningsCollector warningsCollector = new AddToListWarnings();
    private final ModuleDependenciesGraphBuildingVisitor testee = new ModuleDependenciesGraphBuildingVisitor(modules,moduleGraph,OTHER);

    @Test
    public void constructorShouldAddAllModulesToTheModuleGraph() {
        final List<Module> allModules = new ArrayList<>(modules.size()+1);
        allModules.addAll(modules);
        allModules.add(OTHER);
        assertThat(graph.getVertices().containsAll(allModules)).isTrue();
        assertThat(allModules.containsAll(graph.getVertices())).isTrue();
    }

    @Test
    public void constructorShouldRemarkRepeatedModules() {
        final List<Module> repeatedModules = Arrays.asList(
                Module.make("Core", "org.example.core.*").get(),
                Module.make("Core", "org.example.io.*").get()
        );
        final DirectedSparseGraph<Module,ModuleDependency> graph = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph = new JungModuleGraph(graph);
        final WarningsCollector warningsCollector = new AddToListWarnings();
        new ModuleDependenciesGraphBuildingVisitor(repeatedModules,moduleGraph,OTHER,warningsCollector);

        assertThat(constructionWarnings.size()).isEqualTo(1);
        assertThat(constructionWarnings.get(0).name).isEqualTo("Core");
    }

    @Test
    public void applyShouldAddSourceAndDestToTheAppropriateModules() {
        final AccessPoint source = AccessPoint.create(ElementName.fromString("org.example.core.Service"));
        final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.io.FileReader"));

        testee.apply(source,dest,null);

        final Optional<ModuleDependency> moduleDependency = moduleGraph.findDependency(CORE,IO);

        assertThat(moduleDependency.isPresent()).isTrue();
    }

    @Test
    public void applyShouldNotConnectionInUnmatchingElements() {
        final AccessPoint source = AccessPoint.create(ElementName.fromString("NOTORG.example.core.Service"));
        final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.io.FileReader"));

        testee.apply(source,dest,null);

        final Optional<ModuleDependency> moduleDependency = moduleGraph.findDependency(CORE,IO);

        assertThat(moduleDependency.isPresent()).isFalse();
    }

  @Test
  public void applyShouldConnectUnmatchingElementsToOther() {
    final AccessPoint source = AccessPoint.create(ElementName.fromString("NOTORG.example.core.Service"));
    final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.io.FileReader"));

    testee.apply(source,dest,null);
    testee.apply(dest,source,null);

    final Optional<ModuleDependency> moduleDependency1 = moduleGraph.findDependency(OTHER,IO);
    final Optional<ModuleDependency> moduleDependency2 = moduleGraph.findDependency(IO,OTHER);

    assertThat(moduleDependency1.isPresent()).isTrue();
    assertThat(moduleDependency2.isPresent()).isTrue();
  }

    @Test
    public void applyShouldNotAddSelfDependencies() {
        final AccessPoint source = AccessPoint.create(ElementName.fromString("org.example.core.Service"));
        final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.core.FileReader"));

        testee.apply(source,dest,null);

        final Optional<ModuleDependency> moduleDependency = moduleGraph.findDependency(CORE,CORE);

        assertThat(moduleDependency.isPresent()).isFalse();
    }

  @Test
  public void applyShouldNotAddSelfDependencyToOther() {
    final AccessPoint source = AccessPoint.create(ElementName.fromString("NOTORG.example.core.Service"));
    final AccessPoint dest = AccessPoint.create(ElementName.fromString("NOTORG.example.core.FileReader"));

    testee.apply(source,dest,null);

    final Optional<ModuleDependency> moduleDependency = moduleGraph.findDependency(OTHER,OTHER);

    assertThat(moduleDependency.isPresent()).isFalse();
  }

    @Test
    public void applyShouldAddSourceAndDestToMoreModulesIfMoreModuleGlobRegexMatch() {
        final List<Module> repeatedModules = Arrays.asList(
                CORE,
                SUPER_MODULE,
                IO
        );
        final DirectedSparseGraph<Module,ModuleDependency> graph = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph = new JungModuleGraph(graph);
        final ModuleDependenciesGraphBuildingVisitor testee = new ModuleDependenciesGraphBuildingVisitor(repeatedModules,moduleGraph,OTHER);

        final AccessPoint source = AccessPoint.create(ElementName.fromString("org.example.core.Service"));
        final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.io.Component"));


        testee.apply(source,dest,null);

        final Optional<ModuleDependency> moduleDependency1 = moduleGraph.findDependency(CORE,SUPER_MODULE);
        final Optional<ModuleDependency> moduleDependency2 = moduleGraph.findDependency(CORE,IO);
        final Optional<ModuleDependency> moduleDependency3 = moduleGraph.findDependency(SUPER_MODULE,IO);

        assertThat(moduleDependency1.isPresent()).isTrue();
        assertThat(moduleDependency2.isPresent()).isTrue();
        assertThat(moduleDependency3.isPresent()).isTrue();
    }

    @Test
    public void applyShouldAddWarningsIfMoreModuleGlobRegexMatch() {
        final List<Module> repeatedModules = Arrays.asList(
                CORE,
                SUPER_MODULE,
                IO
        );
        final DirectedSparseGraph<Module,ModuleDependency> graph = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph = new JungModuleGraph(graph);
        final ModuleDependenciesGraphBuildingVisitor testee = new ModuleDependenciesGraphBuildingVisitor(repeatedModules,moduleGraph, OTHER, warningsCollector);

        final AccessPoint source = AccessPoint.create(ElementName.fromString("org.example.core.Service"));
        final AccessPoint dest = AccessPoint.create(ElementName.fromString("org.example.io.Component"));


        testee.apply(source,dest,null);

        assertThat(visitWarningsContainPairMatching(source.getElementName(),CORE,SUPER_MODULE)).isTrue();
        assertThat(visitWarningsContainPairMatching(dest.getElementName(),IO,SUPER_MODULE)).isTrue();
    }

    private boolean visitWarningsContainPairMatching(ElementName ap, Module ... modules) {
        boolean match = false;
        for(int i = 0; !match && i < visitWarnings.size(); ++i) {
            final Pair<ElementName,Collection<Module>> pair = visitWarnings.get(i);
            final List<Module> expected = Arrays.asList(modules);
            match = pair.first.equals(ap) && pair.second.containsAll(expected) && expected.containsAll(pair.second);
        }
        return match;
    }
}
