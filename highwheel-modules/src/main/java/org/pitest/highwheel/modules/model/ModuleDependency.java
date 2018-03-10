package org.pitest.highwheel.modules.model;

public final class ModuleDependency {

    public final Module source;
    public final Module dest;
    private int count;

    public ModuleDependency(Module source, Module dest) {
        this.source = source;
        this.dest = dest;
        this.count= 0;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }
}
