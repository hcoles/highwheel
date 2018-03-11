package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.util.base.Function;
import org.pitest.highwheel.util.base.Optional;

import java.util.*;

/**
 * Calculate the Transitive closure of a ModuleGraph using the Floyd-Warshall algorithm
 * Ref Cormen, Thomas H.; Leiserson, Charles E.; Rivest, Ronald L. (1990). Introduction to Algorithms (1st ed.).
 * MIT Press and McGraw-Hill. ISBN 0-262-03141-8. See in particular Section 26.2, "The Floyd–Warshall algorithm"
 * pp. 558–565
 */
public class ModuleGraphTransitiveClosure {

    public static class Difference{
        public final Module source;
        public final Module dest;
        public final int firstDistance;
        public final int secondDistance;

        public Difference(Module source, Module dest, int firstDistance, int secondDistance) {
            this.source = source;
            this.dest = dest;
            this.firstDistance = firstDistance;
            this.secondDistance = secondDistance;
        }
    }

    private final int[][] distanceMatrix;
    private final Map<Module,Integer> indexMap;
    private final Collection<Module> modules;

    public ModuleGraphTransitiveClosure(ModuleGraph moduleGraph, Collection<Module> modules) {
        this.modules = modules;
        distanceMatrix = initialiseSquareMatrixTo(modules.size(),Integer.MAX_VALUE);
        indexMap = createMapModuleIndex(modules);

        initialiseDistanceOneModules(modules,moduleGraph);

        applyFloydWarshallMainIteration();
    }

    private int[][] initialiseSquareMatrixTo(final int size, final int value) {
        final int[][] array = new int[size][size];
        for(int i=0; i < array.length; ++i)
            for(int j=0; j < array.length; ++j)
                array[i][j] = value;
        return array;
    }

    private Map<Module,Integer> createMapModuleIndex(Collection<Module> modules) {
        final Map<Module,Integer> map = new HashMap<Module, Integer>(modules.size());
        int moduleCount = 0;
        for(Module module : modules) {
            map.put(module,moduleCount++);
        }
        return map;
    }

    private void initialiseDistanceOneModules(Collection<Module> modules, ModuleGraph moduleGraph){
        for(Module module : modules) {
            for(Module dependency : moduleGraph.dependencies(module)) {
                distanceMatrix[indexMap.get(module)][indexMap.get(dependency)] = 1;
            }
        }
    }

    private void applyFloydWarshallMainIteration() {
        for(int i = 0; i < distanceMatrix.length; ++i) {
            for(int j = 0; j < distanceMatrix.length; ++j) {
                for(int k = 0; k < distanceMatrix.length; ++k) {
                    int distanceIJ = distanceMatrix[i][j];
                    int distanceIK = distanceMatrix[i][k];
                    int distanceKJ = distanceMatrix[k][j];
                    if(distanceIJ == Integer.MAX_VALUE && distanceIK < Integer.MAX_VALUE && distanceKJ < Integer.MAX_VALUE) {
                        distanceMatrix[i][j] = distanceIK + distanceKJ;
                    } else if(distanceIJ < Integer.MAX_VALUE && distanceIK < Integer.MAX_VALUE && distanceKJ < Integer.MAX_VALUE) {
                        distanceMatrix[i][j] = Math.min(distanceIJ,distanceIK + distanceKJ);
                    }
                }
            }
        }
    }

    public Boolean isReachable(Module vertex1, Module vertex2) {
        final Function<Integer,Boolean> smallerThanMaxInt = new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer argument) {
                return argument < Integer.MAX_VALUE;
            }
        };
        return minimumDistance(vertex1,vertex2).map(smallerThanMaxInt).orElse(false);
    }

    public boolean same(ModuleGraphTransitiveClosure other) {
        final Function<List<Difference>,Boolean> emptyList = new Function<List<Difference>, Boolean>() {
            @Override
            public Boolean apply(List<Difference> argument) {
                return argument.isEmpty();
            }
        };
        return diff(other).map(emptyList).orElse(false);
    }

    public Optional<List<Difference>> diff(ModuleGraphTransitiveClosure other) {
        if(!modules.containsAll(other.modules) || !other.modules.containsAll(modules))
            return Optional.empty();
        final List<Difference> differences = new ArrayList<Difference>();
        for(Module i : modules) {
            for (Module j : modules) {
                int thisI = indexMap.get(i),
                        thisJ = indexMap.get(j),
                        otherI = indexMap.get(i),
                        otherJ = indexMap.get(j);
                if(distanceMatrix[thisI][thisJ] != other.distanceMatrix[otherI][otherJ])
                    differences.add(new Difference(i,j,distanceMatrix[thisI][thisJ],
                            other.distanceMatrix[otherI][otherJ]));
            }
        }
        return Optional.of(differences);
    }

    public Optional<Integer> minimumDistance(Module vertex1, Module vertex2) {
        if(indexMap.get(vertex1) == null || indexMap.get(vertex2) == null)
            return Optional.empty();
        else
            return Optional.of(distanceMatrix[indexMap.get(vertex1)][indexMap.get(vertex2)]);
    }
}
