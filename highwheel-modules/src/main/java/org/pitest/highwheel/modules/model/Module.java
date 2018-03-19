package org.pitest.highwheel.modules.model;

import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.util.GlobToRegex;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Module {

    public final String name;
    public final List<String> patternLiterals;
    private final List<Pattern> patterns;

    private Module(String name, Stream<String> patternLiteral) {
        this.name = name;
        this.patternLiterals = patternLiteral.collect(Collectors.toList());
        this.patterns = patternLiterals.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    public static Optional<Module> make(String moduleName, String ... globs) {
        return make(moduleName,Arrays.asList(globs));
    }

    public static Optional<Module> make(String moduleName, List<String> globs) {
      try {
        return Optional.of(new Module(moduleName, globs.stream().map(GlobToRegex::convertGlobToRegex)));
      } catch(PatternSyntaxException e) {
        return Optional.empty();
      }
    }

    public boolean contains(ElementName elementName) {
        return patterns.stream().anyMatch((p) -> p.matcher(elementName.asJavaName()).matches());
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", patternLiteral='" + patternLiterals + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        return Objects.equals(this.name,module.name) &&
                Objects.equals(this.patternLiterals, module.patternLiterals);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + patternLiterals.hashCode();
        return result;
    }
}
