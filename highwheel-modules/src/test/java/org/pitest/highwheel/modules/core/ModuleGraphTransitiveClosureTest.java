package org.pitest.highwheel.modules.core;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.junit.Before;
import org.junit.Test;
import org.pitest.highwheel.modules.externaladapters.JungModuleGraph;
import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleDependency;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class ModuleGraphTransitiveClosureTest {
    private final Module CORE = Module.make("Core", "org.example.core.*").get();
    private final Module FACADE = Module.make("Facade", "org.example.core.external.*").get();
    private final Module IO = Module.make("IO", "org.example.io.*").get();
    private final Module COMMONS = Module.make("Commons", "org.example.commons.*").get();
    private final Module ENDPOINTS = Module.make("Endpoints", "org.example.endpoints.*").get();
    private final Module MAIN = Module.make("Main","org.example.Main").get();

    private final List<Module> modules = Arrays.asList(CORE,FACADE,IO,COMMONS,ENDPOINTS,MAIN);
    private final DirectedSparseGraph<Module,ModuleDependency> graph = new DirectedSparseGraph<Module, ModuleDependency>();
    private final JungModuleGraph moduleGraph = new JungModuleGraph(graph);
    private ModuleGraphTransitiveClosure testee;


    @Before
    public void setUp() {
        for(Module module: modules) {
            moduleGraph.addModule(module);
        }
        moduleGraph.addDependency(CORE,COMMONS);
        moduleGraph.addDependency(FACADE,CORE);
        moduleGraph.addDependency(IO,COMMONS);
        moduleGraph.addDependency(ENDPOINTS,COMMONS);
        moduleGraph.addDependency(ENDPOINTS,FACADE);
        moduleGraph.addDependency(IO,CORE);
        moduleGraph.addDependency(MAIN,ENDPOINTS);
        moduleGraph.addDependency(MAIN,IO);
        moduleGraph.addDependency(MAIN,CORE);
        testee = new ModuleGraphTransitiveClosure(moduleGraph,modules);
    }
    
    @Test
    public void minimumDistanceShouldBeMaxIntForNotDependentModules() {
        assertThat(testee.minimumDistance(COMMONS,COMMONS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(CORE,CORE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(FACADE,FACADE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(IO,IO).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(ENDPOINTS,ENDPOINTS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(MAIN,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(COMMONS,CORE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(COMMONS,FACADE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(COMMONS,IO).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(COMMONS,ENDPOINTS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(COMMONS,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(CORE,FACADE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(CORE,IO).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(CORE,ENDPOINTS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(CORE,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(FACADE,IO).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(FACADE,ENDPOINTS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(FACADE,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(IO,ENDPOINTS).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(IO,FACADE).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(IO,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(ENDPOINTS,IO).get()).isEqualTo(Integer.MAX_VALUE);
        assertThat(testee.minimumDistance(ENDPOINTS,MAIN).get()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void minimumDistanceShouldBeTheExpectedOneForDependentModules() {
        assertThat(testee.minimumDistance(CORE,COMMONS).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(FACADE,CORE).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(FACADE,COMMONS).get()).isEqualTo(2);
        assertThat(testee.minimumDistance(IO,COMMONS).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(IO,CORE).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(ENDPOINTS,FACADE).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(ENDPOINTS,CORE).get()).isEqualTo(2);
        assertThat(testee.minimumDistance(ENDPOINTS,COMMONS).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(MAIN,CORE).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(MAIN,IO).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(MAIN,ENDPOINTS).get()).isEqualTo(1);
        assertThat(testee.minimumDistance(MAIN,FACADE).get()).isEqualTo(2);
        assertThat(testee.minimumDistance(MAIN,COMMONS).get()).isEqualTo(2);
    }

    @Test
    public void isReachableShouldBeTrueIffMinimumDistanceIsGreaterThanZero() {
        for(Module module1 : modules) {
            for(Module module2: modules) {
                assertThat(testee.isReachable(module1,module2) && testee.minimumDistance(module1,module2).get() < Integer.MAX_VALUE ||
                        ! testee.isReachable(module1,module2) && testee.minimumDistance(module1,module2).get() == Integer.MAX_VALUE).isTrue();
            }
        }
    }

    @Test
    public void sameShouldReturnTrueOnTransitiveClosureThatAreEqual() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("Main","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);

        assertThat(testee.same(otherTestee)).isTrue();
    }

    @Test
    public void sameShouldReturnFalseOnTransitiveClosureWithDifferentModules() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("DIFFERENT NAME","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);

        assertThat(testee.same(otherTestee)).isFalse();
    }

    @Test
    public void sameShouldReturnFalseOnTransitiveClosureWithDifferentDependencies() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("Main","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        //moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);

        assertThat(testee.same(otherTestee)).isFalse();
    }

    @Test
    public void diffShouldReturnEmptyListOnTransitiveClosureThatAreEqual() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("Main","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);

        assertThat(testee.diff(otherTestee).get().isEmpty()).isTrue();
    }

    @Test
    public void diffShouldReturnEmptyOnTransitiveClosureWithDifferentModules() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("DIFFERENT NAME","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);

        assertThat(testee.diff(otherTestee).isPresent()).isFalse();
    }

    @Test
    public void diffShouldReturnListOfDifferencesOnTransitiveClosureWithDifferentDependencies() {
        final Module CORE_2 = Module.make("Core", "org.example.core.*").get();
        final Module FACADE_2 = Module.make("Facade", "org.example.core.external.*").get();
        final Module IO_2 = Module.make("IO", "org.example.io.*").get();
        final Module COMMONS_2 = Module.make("Commons", "org.example.commons.*").get();
        final Module ENDPOINTS_2 = Module.make("Endpoints", "org.example.endpoints.*").get();
        final Module MAIN_2 = Module.make("Main","org.example.Main").get();

        final List<Module> modules_2 = Arrays.asList(CORE_2,FACADE_2,IO_2,COMMONS_2,ENDPOINTS_2,MAIN_2);
        final DirectedSparseGraph<Module,ModuleDependency> graph_2 = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph moduleGraph_2 = new JungModuleGraph(graph_2);

        for(Module module: modules_2) {
            moduleGraph_2.addModule(module);
        }
        moduleGraph_2.addDependency(CORE_2,COMMONS_2);
        moduleGraph_2.addDependency(FACADE_2,CORE_2);
        moduleGraph_2.addDependency(IO_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,COMMONS_2);
        moduleGraph_2.addDependency(ENDPOINTS_2,FACADE_2);
        moduleGraph_2.addDependency(IO_2,CORE_2);
        moduleGraph_2.addDependency(MAIN_2,ENDPOINTS_2);
        moduleGraph_2.addDependency(MAIN_2,IO_2);
        //moduleGraph_2.addDependency(MAIN_2,CORE_2);

        final ModuleGraphTransitiveClosure otherTestee = new ModuleGraphTransitiveClosure(moduleGraph_2,modules_2);
        final List<ModuleGraphTransitiveClosure.Difference> differences = testee.diff(otherTestee).get();

        assertThat(differences.size()).isEqualTo(1);

        assertThat(differences.get(0).source).isEqualTo(MAIN);
        assertThat(differences.get(0).dest).isEqualTo(CORE);
        assertThat(differences.get(0).firstDistance).isEqualTo(1);
        assertThat(differences.get(0).secondDistance).isEqualTo(2);
    }
}
