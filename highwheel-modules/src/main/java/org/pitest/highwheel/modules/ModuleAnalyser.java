package org.pitest.highwheel.modules;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.modules.core.ModuleDependenciesGraphBuildingVisitor;
import org.pitest.highwheel.modules.core.ModuleGraphTransitiveClosure;
import org.pitest.highwheel.modules.externaladapters.JungModuleGraph;
import org.pitest.highwheel.modules.model.*;
import org.pitest.highwheel.modules.model.rules.Dependency;
import org.pitest.highwheel.modules.model.rules.NoStrictDependency;
import org.pitest.highwheel.modules.AnalyserModel.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ModuleAnalyser {


    private final ClassParser classParser;


    public ModuleAnalyser(final ClassParser classParser) {
        this.classParser = classParser;
    }

    public StrictAnalysisResult analyseStrict(final ClasspathRoot root, final Definition definition) {
        final Module other = Module.make("(other)", "").get();
        final Collection<Module> modules = definition.modules;
        if(modules.isEmpty())
            throw new AnalyserException("No modules provided in definition");
        final JungModuleGraph specModuleGraph = initialiseSpecificationGraph(modules,definition.dependencies);
        final JungModuleGraph actualModuleGraph = initialiseEmptyGraph();

        runAnalysis(modules,actualModuleGraph,root,other);

        final ModuleGraphTransitiveClosure specTransitiveClosure = new ModuleGraphTransitiveClosure(specModuleGraph,append(modules,other));
        final ModuleGraphTransitiveClosure actualTransitiveClosure = new ModuleGraphTransitiveClosure(actualModuleGraph,append(modules,other));

        final List<DependencyViolation> dependencyViolations = getDependencyViolations(specTransitiveClosure.diffPath(actualTransitiveClosure).get(),other);
        final List<NoStrictDependencyViolation> noStrictDependencyViolations = getNoDirectDependecyViolations(actualTransitiveClosure,definition.noStrictDependencies,other);
        final List<Metrics> metrics = getMetrics(actualModuleGraph,modules,actualModuleGraph,other);

        return new StrictAnalysisResult(dependencyViolations, noStrictDependencyViolations,metrics);
    }

    private Collection<Module> append(Collection<Module> modules, Module module) {
        final List<Module> result = new ArrayList<>(modules);
        result.add(module);
        return result;
    }



    private JungModuleGraph initialiseSpecificationGraph(Collection<Module> modules, Collection<Dependency> dependencies) {
        final DirectedGraph<Module,ModuleDependency> specGraph = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph specModuleGraph = new JungModuleGraph(specGraph);

        for(Module module: modules) {
            specModuleGraph.addModule(module);
        }
        for(Dependency dep: dependencies) {
            specModuleGraph.addDependency(dep.source,dep.dest);
        }

        return specModuleGraph;
    }

    private JungModuleGraph initialiseEmptyGraph() {
        final DirectedGraph<Module,ModuleDependency> actualGraph = new DirectedSparseGraph<Module, ModuleDependency>();
        return new JungModuleGraph(actualGraph);
    }

    private void runAnalysis(Collection<Module> modules, ModuleGraph moduleGraph, ClasspathRoot root, Module other) {
        final ModuleDependenciesGraphBuildingVisitor visitor = new ModuleDependenciesGraphBuildingVisitor(modules,moduleGraph,other);

        try {
            classParser.parse(root, visitor);
        } catch(IOException e){
            throw new AnalyserException(e);
        }
    }

    private List<DependencyViolation> getDependencyViolations(List<ModuleGraphTransitiveClosure.PathDifference> differences, Module other) {
        final List<DependencyViolation> dependencyViolations = new ArrayList<DependencyViolation>(differences.size());
        for(ModuleGraphTransitiveClosure.PathDifference difference : differences) {
            if(! difference.source.equals(other) && !difference.dest.equals(other)) {
                dependencyViolations.add(new DependencyViolation(difference.source.name, difference.dest.name, getNames(difference.firstPath), getNames(difference.secondPath)));
            }
        }
        return dependencyViolations;
    }

    private List<NoStrictDependencyViolation> getNoDirectDependecyViolations(ModuleGraphTransitiveClosure transitiveClosure, Collection<NoStrictDependency> rules, Module other) {
        final List<NoStrictDependencyViolation> noStrictDependencyViolations = new ArrayList<NoStrictDependencyViolation>();
        for(NoStrictDependency rule : rules) {
            if(!rule.source.equals(other) && !rule.dest.equals(other) && transitiveClosure.minimumDistance(rule.source,rule.dest).get() == 1) {
                noStrictDependencyViolations.add(new NoStrictDependencyViolation(rule.source.name,rule.dest.name));
            }
        }
        return noStrictDependencyViolations;
    }

    private List<Metrics> getMetrics(ModuleMetrics moduleMetrics, Collection<Module> modules, ModuleGraph graph, Module other) {
        final List<Metrics> metrics = new ArrayList<Metrics>(modules.size());
        for(Module module : modules) {
            metrics.add(new Metrics(module.name,moduleMetrics.fanInOf(module).get() +
                (graph.findDependency(other,module).isPresent() ? -1 : 0),
                moduleMetrics.fanOutOf(module).get() + (
                    graph.findDependency(module,other).isPresent() ? -1 :0)));
        }
        return metrics;
    }

    private static List<String> getNames(Collection<Module> modules) {
        final List<String> result = new ArrayList<String>(modules.size());
        for(Module module: modules) {
            result.add(module.name);
        }
        return result;
    }

    public LooseAnalysisResult analyseLoose(final ClasspathRoot root, final Definition definition) {
        final Collection<Module> modules = definition.modules;
        final Module other = Module.make("(other)", "").get();
        if(modules.isEmpty())
            throw new AnalyserException("No modules provided in definition");
        final JungModuleGraph actualModuleGraph = initialiseEmptyGraph();

        runAnalysis(modules,actualModuleGraph,root,other);

        final ModuleGraphTransitiveClosure actualTransitiveClosure = new ModuleGraphTransitiveClosure(actualModuleGraph,append(modules,other));

        final List<AbsentDependencyViolation> absentDependencyViolations = getAbsentDependencies(modules,actualTransitiveClosure,definition.dependencies,other);
        final List<UndesiredDependencyViolation> undesiredDependencyViolations = getUndesiredDependecies(actualTransitiveClosure,definition.noStrictDependencies,other);

        return new LooseAnalysisResult(absentDependencyViolations,undesiredDependencyViolations,getMetrics(actualModuleGraph,modules,actualModuleGraph,other));
    }

    private List<AbsentDependencyViolation> getAbsentDependencies(Collection<Module> modules,
                                                                  ModuleGraphTransitiveClosure transitiveClosure,
                                                                  Collection<Dependency> dependencies, Module other) {
        final List<AbsentDependencyViolation> dependencyViolations = new ArrayList<AbsentDependencyViolation>();
        for(Dependency dependency : dependencies) {
            if(!dependency.source.equals(other) && !dependency.equals(other) &&!transitiveClosure.isReachable(dependency.source,dependency.dest)) {
                dependencyViolations.add(new AbsentDependencyViolation(dependency.source.name,dependency.dest.name));
            }
        }

        return dependencyViolations;
    }

    private List<UndesiredDependencyViolation> getUndesiredDependecies(ModuleGraphTransitiveClosure transitiveClosure, Collection<NoStrictDependency> noStrictDependencies, Module other) {
        final List<UndesiredDependencyViolation> undesiredDependencyViolations = new ArrayList<UndesiredDependencyViolation>();
        for(NoStrictDependency noStrictDependency: noStrictDependencies){
            if(!noStrictDependency.source.equals(other) && !noStrictDependency.dest.equals(other) && transitiveClosure.isReachable(noStrictDependency.source,noStrictDependency.dest)) {
                undesiredDependencyViolations.add(new UndesiredDependencyViolation(noStrictDependency.source.name,
                        noStrictDependency.dest.name,
                        getNames(
                                transitiveClosure.minimumDistancePath(
                                        noStrictDependency.source,noStrictDependency.dest
                                )
                        )
                ));
            }
        }
        return undesiredDependencyViolations;
    }
}
