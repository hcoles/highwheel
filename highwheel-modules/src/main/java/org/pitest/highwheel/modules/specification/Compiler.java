package org.pitest.highwheel.modules.specification;

import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.rules.Dependency;
import org.pitest.highwheel.modules.model.rules.NoDirectDependency;
import org.pitest.highwheel.modules.model.rules.Rule;
import org.pitest.highwheel.util.base.Optional;

import java.util.*;

public class Compiler {

    public static class Definition {
        public final Collection<Module> modules;
        public final Collection<Rule> rules;

        public Definition(Collection<Module> modules, Collection<Rule> rules) {
            this.modules = modules;
            this.rules = rules;
        }

        @Override
        public String toString() {
            return "Definition{" +
                    "modules=" + modules +
                    ", rules=" + rules +
                    '}';
        }
    }

    public Definition compile(SyntaxTree.Definition definition) {
        final Map<String,Module> modules = new HashMap<String, Module>(definition.moduleDefinitions.size());
        for(SyntaxTree.ModuleDefinition moduleDefinition : definition.moduleDefinitions) {
            final Optional<Module> optionalModule = Module.make(moduleDefinition.moduleName,moduleDefinition.moduleRegex);
            if(!optionalModule.isPresent()) {
                throw new CompilerException(String.format("Regular expression '%s' of module '%s' is not well defined",moduleDefinition.moduleRegex,moduleDefinition.moduleName));
            } else if(modules.get(moduleDefinition.moduleName) != null) {
                throw new CompilerException(String.format("Module '%s' has already been defined", moduleDefinition.moduleName));
            } else {
                modules.put(moduleDefinition.moduleName,optionalModule.get());
            }
        }

        final List<Rule> rules = new ArrayList<Rule>();
        for(SyntaxTree.Rule ruleDefinition: definition.rules) {
            if(ruleDefinition instanceof SyntaxTree.ChainDependencyRule) {
                SyntaxTree.ChainDependencyRule chainDependencyRule = (SyntaxTree.ChainDependencyRule) ruleDefinition;
                for(int i = 0; i < chainDependencyRule.moduleNameChain.size() -1; ++i) {
                    final String current = chainDependencyRule.moduleNameChain.get(i);
                    final String next = chainDependencyRule.moduleNameChain.get(i+1);
                    if(modules.get(current) == null) {
                        throw new CompilerException(String.format("Module '%s' referenced in rule '%s' has not been defined", current, join(" -> ", chainDependencyRule.moduleNameChain)));
                    } else if(modules.get(next) == null) {
                        throw new CompilerException(String.format("Module '%s' referenced in rule '%s' has not been defined", next, join(" -> ", chainDependencyRule.moduleNameChain)));
                    } else {
                        rules.add(new Dependency(modules.get(current),modules.get(next)));
                    }
                }
            } else if(ruleDefinition instanceof SyntaxTree.NoDependentRule){
                SyntaxTree.NoDependentRule noDependentRule = (SyntaxTree.NoDependentRule) ruleDefinition;
                if(modules.get(noDependentRule.left) == null) {
                    throw new CompilerException(String.format("Module '%s' referenced in rule '%s' has not been defined", noDependentRule.left, join(" -/-> ", Arrays.asList(noDependentRule.left,noDependentRule.right))));
                } else if(modules.get(noDependentRule.right) == null) {
                    throw new CompilerException(String.format("Module '%s' referenced in rule '%s' has not been defined", noDependentRule.right, join(" -/-> ", Arrays.asList(noDependentRule.left,noDependentRule.right))));
                } else {
                    rules.add(new NoDirectDependency(modules.get(noDependentRule.left),modules.get(noDependentRule.right)));
                }
            }
        }
        return new Definition(modules.values(),rules);
    }

    private static <T> String join(String separator, Iterable<T> iterable) {
        final StringBuilder buff = new StringBuilder("");
        String sep = "";
        for(T item : iterable) {
            buff.append(sep).append(item.toString());
            sep = separator;
        }
        return buff.toString();
    }
}
