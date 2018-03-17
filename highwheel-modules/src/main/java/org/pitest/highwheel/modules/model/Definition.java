package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.modules.model.rules.Dependency;
import org.pitest.highwheel.modules.model.rules.NoDirectDependency;

import java.util.Collection;

public class Definition {
    public final Collection<Module> modules;
    public final Collection<Dependency> dependencies;
    public final Collection<NoDirectDependency> noDirectDependencies;

    public Definition(Collection<Module> modules, Collection<Dependency> dependencies, Collection<NoDirectDependency> noDirectDependencies) {
        this.modules = modules;
        this.dependencies = dependencies;
        this.noDirectDependencies = noDirectDependencies;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "modules=" + modules +
                ", dependencies=" + dependencies +
                ", noDirectDependencies=" + noDirectDependencies +
                '}';
    }
}