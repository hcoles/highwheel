package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.util.GlobToRegex;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class Module {

    public final String name;
    public final String patternLiteral;
    private final Pattern pattern;

    private Module(String name, String patternLiteral) {
        this.name = name;
        this.patternLiteral = patternLiteral;
        this.pattern = Pattern.compile(patternLiteral);
    }

    public static Optional<Module> make(String moduleName, String glob) {
        try {
            return Optional.of(new Module(moduleName, GlobToRegex.convertGlobToRegex(glob)));
        } catch(PatternSyntaxException e) {
            return Optional.empty();
        }
    }

    public boolean contains(ElementName elementName) {
        return pattern.matcher(elementName.asJavaName()).matches();
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", patternLiteral='" + patternLiteral + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        return Objects.equals(this.name,module.name) &&
                Objects.equals(this.patternLiteral, module.patternLiteral);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + patternLiteral.hashCode();
        return result;
    }
}
