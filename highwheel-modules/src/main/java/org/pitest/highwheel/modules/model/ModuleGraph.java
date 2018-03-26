package org.pitest.highwheel.modules.model;

import java.util.Collection;
import java.util.Optional;

public interface ModuleGraph {
    Optional<ModuleDependency> findDependency(Module vertex1, Module vertex2);
    void addDependency(Module vertex1, Module vertex2);
    void addModule(Module vertex);
    Collection<Module> dependencies(Module vertex);
}
