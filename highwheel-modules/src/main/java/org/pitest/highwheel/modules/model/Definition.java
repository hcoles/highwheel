package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.modules.model.rules.Dependency;
import org.pitest.highwheel.modules.model.rules.NoStrictDependency;

import java.util.Collection;

public class Definition {
    public final Collection<Module> modules;
    public final Collection<Dependency> dependencies;
    public final Collection<NoStrictDependency> noStrictDependencies;

    public Definition(Collection<Module> modules, Collection<Dependency> dependencies, Collection<NoStrictDependency> noStrictDependencies) {
        this.modules = modules;
        this.dependencies = dependencies;
        this.noStrictDependencies = noStrictDependencies;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "modules=" + modules +
                ", dependencies=" + dependencies +
                ", noStrictDependencies=" + noStrictDependencies +
                '}';
    }
}