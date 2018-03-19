package org.pitest.highwheel.modules.externaladapters;

import edu.uci.ics.jung.graph.DirectedGraph;
import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleDependency;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.modules.model.ModuleMetrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class JungModuleGraph implements ModuleGraph, ModuleMetrics {

    private final DirectedGraph<Module,ModuleDependency> graph;

    public JungModuleGraph(DirectedGraph<Module,ModuleDependency> graph) {
        this.graph = graph;
    }

    @Override
    public Optional<ModuleDependency> findDependency(Module vertex1, Module vertex2) {
        return Optional.ofNullable(graph.findEdge(vertex1, vertex2));
    }

    @Override
    public void addDependency(final Module vertex1, final Module vertex2) {
        if(graph.getVertices().containsAll(Arrays.asList(vertex1,vertex2))) {
            Optional<ModuleDependency> dependencyOptional = Optional.ofNullable(graph.findEdge(vertex1,vertex2));
            final ModuleDependency moduleDependency = dependencyOptional.orElseGet(() -> {
                final ModuleDependency dependency= new ModuleDependency(vertex1,vertex2);
                graph.addEdge(dependency,vertex1,vertex2);
                return dependency;
            });
            moduleDependency.incrementCount();
        }
    }

    @Override
    public void addModule(Module vertex) {
        graph.addVertex(vertex);
    }

    @Override
    public Collection<Module> dependencies(Module vertex) {
        return Optional.ofNullable(graph.getSuccessors(vertex)).orElse(Collections.<Module>emptyList());
    }

    @Override
    public Optional<Integer> fanInOf(Module module) {
        if(!graph.containsVertex(module))
            return Optional.empty();
        else {
            final Optional<ModuleDependency> self = findDependency(module,module);
            return Optional.of(graph.inDegree(module) - self.map((a) -> 1).orElse(0));
        }
    }

    @Override
    public Optional<Integer> fanOutOf(Module module) {
        if(!graph.containsVertex(module))
            return Optional.empty();
        else {
            final Optional<ModuleDependency> self = findDependency(module, module);
            return Optional.of(graph.outDegree(module) - self.map((a)->1).orElse(0));
        }
    }
}
