package org.pitest.highwheel.modules.model;

public final class ModuleDependency {

    private final Module source;
    private final Module dest;
    private int count;

    public ModuleDependency(Module source, Module dest) {
        this.source = source;
        this.dest = dest;
        this.count= 0;
    }
}
