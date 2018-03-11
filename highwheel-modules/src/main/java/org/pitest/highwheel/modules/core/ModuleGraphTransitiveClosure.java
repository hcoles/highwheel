package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleGraph;
import org.pitest.highwheel.util.base.Function;
import org.pitest.highwheel.util.base.Optional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculate the Transitive closure of a ModuleGraph using the Floyd-Warshall algorithm
 * Ref Cormen, Thomas H.; Leiserson, Charles E.; Rivest, Ronald L. (1990). Introduction to Algorithms (1st ed.).
 * MIT Press and McGraw-Hill. ISBN 0-262-03141-8. See in particular Section 26.2, "The Floyd–Warshall algorithm"
 * pp. 558–565
 */
public class ModuleGraphTransitiveClosure {

    private final int[][] distanceMatrix;
    private final Map<Module,Integer> indexMap;

    public ModuleGraphTransitiveClosure(ModuleGraph moduleGraph, Collection<Module> modules) {
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
        return false;
    }

    public Optional<Integer> minimumDistance(Module vertex1, Module vertex2) {
        if(indexMap.get(vertex1) == null || indexMap.get(vertex2) == null)
            return Optional.empty();
        else
            return Optional.of(distanceMatrix[indexMap.get(vertex1)][indexMap.get(vertex2)]);
    }
}
