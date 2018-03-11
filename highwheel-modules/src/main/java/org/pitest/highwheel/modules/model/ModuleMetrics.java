package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.util.base.Optional;

public interface ModuleMetrics {
    Optional<Integer> fanInOf(Module module);
    Optional<Integer> fanOutOf(Module module);
}
