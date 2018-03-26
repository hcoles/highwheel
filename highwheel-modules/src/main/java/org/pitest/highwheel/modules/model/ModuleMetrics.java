package org.pitest.highwheel.modules.model;

import java.util.Optional;

public interface ModuleMetrics {
    Optional<Integer> fanInOf(Module module);
    Optional<Integer> fanOutOf(Module module);
}
