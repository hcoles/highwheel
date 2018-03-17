package org.pitest.highwheel.modules.model.rules;

import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.util.validation.Objects;

public class NoDirectDependency implements Rule {
    public final Module source;
    public final Module dest;

    public NoDirectDependency(Module source, Module dest) {
        this.source = source;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return String.format("%s -/-> %s",source.name,dest.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoDirectDependency that = (NoDirectDependency) o;

        return Objects.equals(this.source,that.source) &&
                Objects.equals(this.dest,that.dest);
    }
}
