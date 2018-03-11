package org.pitest.highwheel.modules.externaladapters;

import edu.uci.ics.jung.graph.DirectedGraph;
import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleDependency;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.modules.model.ModuleMetrics;
import org.pitest.highwheel.util.base.Function;
import org.pitest.highwheel.util.base.Optional;
import org.pitest.highwheel.util.base.Supplier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        final Supplier<ModuleDependency> buildAndAddDependency = new Supplier<ModuleDependency>() {
            @Override
            public ModuleDependency supply() {
                final ModuleDependency dependency= new ModuleDependency(vertex1,vertex2);
                graph.addEdge(dependency,vertex1,vertex2);
                return dependency;
            }
        };
        if(graph.getVertices().containsAll(Arrays.asList(vertex1,vertex2))) {
            Optional<ModuleDependency> dependencyOptional = Optional.ofNullable(graph.findEdge(vertex1,vertex2));
            final ModuleDependency moduleDependency = dependencyOptional.orElseGet(buildAndAddDependency);
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
            return Optional.of(graph.inDegree(module) - self.map(constantOne).orElse(0));
        }
    }

    private Function<ModuleDependency,Integer> constantOne = new Function<ModuleDependency, Integer>() {
        @Override
        public Integer apply(ModuleDependency argument) {
            return 1;
        }
    };

    @Override
    public Optional<Integer> fanOutOf(Module module) {
        if(!graph.containsVertex(module))
            return Optional.empty();
        else {
            final Optional<ModuleDependency> self = findDependency(module, module);
            return Optional.of(graph.outDegree(module) - self.map(constantOne).orElse(0));
        }
    }
}
