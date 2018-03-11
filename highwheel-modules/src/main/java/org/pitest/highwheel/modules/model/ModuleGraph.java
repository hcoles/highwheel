package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.util.base.Optional;

import java.util.Collection;

public interface ModuleGraph {
    Optional<ModuleDependency> findDependency(Module vertex1, Module vertex2);
    void addDependency(Module vertex1, Module vertex2);
    void addModule(Module vertex);
    Collection<Module> dependencies(Module vertex);
}
