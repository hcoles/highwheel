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
        final Collection<Module> modules = definition.modules;
        if(modules.isEmpty())
            throw new AnalyserException("No modules provided in definition");
        final JungModuleGraph specModuleGraph = initialiseSpecificationGraph(modules,definition.dependencies);
        final JungModuleGraph actualModuleGraph = initialiseEmptyGraph(modules);

        runAnalysis(modules,actualModuleGraph,root);

        final ModuleGraphTransitiveClosure specTransitiveClosure = new ModuleGraphTransitiveClosure(specModuleGraph,modules);
        final ModuleGraphTransitiveClosure actualTransitiveClosure = new ModuleGraphTransitiveClosure(actualModuleGraph,modules);

        final List<DependencyViolation> dependencyViolations = getDependencyViolations(specTransitiveClosure.diffPath(actualTransitiveClosure).get());
        final List<NoStrictDependencyViolation> noStrictDependencyViolations = getNoDirectDependecyViolations(actualTransitiveClosure,definition.noStrictDependencies);
        final List<Metrics> metrics = getMetrics(actualModuleGraph,modules);

        return new StrictAnalysisResult(dependencyViolations, noStrictDependencyViolations,metrics);
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

    private JungModuleGraph initialiseEmptyGraph(Collection<Module> modules) {
        final DirectedGraph<Module,ModuleDependency> actualGraph = new DirectedSparseGraph<Module, ModuleDependency>();
        final JungModuleGraph actualModuleGraph = new JungModuleGraph(actualGraph);

        for(Module module: modules) {
            actualModuleGraph.addModule(module);
        }

        return actualModuleGraph;
    }

    private void runAnalysis(Collection<Module> modules, ModuleGraph moduleGraph, ClasspathRoot root) {
        final ModuleDependenciesGraphBuildingVisitor visitor = new ModuleDependenciesGraphBuildingVisitor(modules,moduleGraph);

        try {
            classParser.parse(root, visitor);
        } catch(IOException e){
            throw new AnalyserException(e);
        }
    }

    private List<DependencyViolation> getDependencyViolations(List<ModuleGraphTransitiveClosure.PathDifference> differences) {
        final List<DependencyViolation> dependencyViolations = new ArrayList<DependencyViolation>(differences.size());
        for(ModuleGraphTransitiveClosure.PathDifference difference : differences) {
            dependencyViolations.add(new DependencyViolation(difference.source.name,difference.dest.name,getNames(difference.firstPath),getNames(difference.secondPath)));
        }
        return dependencyViolations;
    }

    private List<NoStrictDependencyViolation> getNoDirectDependecyViolations(ModuleGraphTransitiveClosure transitiveClosure, Collection<NoStrictDependency> rules) {
        final List<NoStrictDependencyViolation> noStrictDependencyViolations = new ArrayList<NoStrictDependencyViolation>();
        for(NoStrictDependency rule : rules) {
            if(transitiveClosure.minimumDistance(rule.source,rule.dest).get() == 1) {
                noStrictDependencyViolations.add(new NoStrictDependencyViolation(rule.source.name,rule.dest.name));
            }
        }
        return noStrictDependencyViolations;
    }

    private List<Metrics> getMetrics(ModuleMetrics moduleMetrics, Collection<Module> modules) {
        final List<Metrics> metrics = new ArrayList<Metrics>(modules.size());
        for(Module module : modules) {
            metrics.add(new Metrics(module.name,moduleMetrics.fanInOf(module).get(),moduleMetrics.fanOutOf(module).get()));
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
        if(modules.isEmpty())
            throw new AnalyserException("No modules provided in definition");
        final JungModuleGraph actualModuleGraph = initialiseEmptyGraph(modules);

        runAnalysis(modules,actualModuleGraph,root);

        final ModuleGraphTransitiveClosure actualTransitiveClosure = new ModuleGraphTransitiveClosure(actualModuleGraph,modules);

        final List<AbsentDependencyViolation> absentDependencyViolations = getAbsentDependencies(modules,actualTransitiveClosure,definition.dependencies);
        final List<UndesiredDependencyViolation> undesiredDependencyViolations = getUndesiredDependecies(actualTransitiveClosure,definition.noStrictDependencies);

        return new LooseAnalysisResult(absentDependencyViolations,undesiredDependencyViolations,getMetrics(actualModuleGraph,modules));
    }

    private List<AbsentDependencyViolation> getAbsentDependencies(Collection<Module> modules,
                                                                  ModuleGraphTransitiveClosure transitiveClosure,
                                                                  Collection<Dependency> dependencies) {
        final List<AbsentDependencyViolation> dependencyViolations = new ArrayList<AbsentDependencyViolation>();
        for(Dependency dependency : dependencies) {
            if(!transitiveClosure.isReachable(dependency.source,dependency.dest)) {
                dependencyViolations.add(new AbsentDependencyViolation(dependency.source.name,dependency.dest.name));
            }
        }

        return dependencyViolations;
    }

    private List<UndesiredDependencyViolation> getUndesiredDependecies(ModuleGraphTransitiveClosure transitiveClosure, Collection<NoStrictDependency> noStrictDependencies) {
        final List<UndesiredDependencyViolation> undesiredDependencyViolations = new ArrayList<UndesiredDependencyViolation>();
        for(NoStrictDependency noStrictDependency: noStrictDependencies){
            if(transitiveClosure.isReachable(noStrictDependency.source,noStrictDependency.dest)) {
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
