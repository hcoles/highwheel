package org.pitest.highwheel.modules.core;

import org.pitest.highwheel.modules.model.Module;
import org.pitest.highwheel.modules.model.ModuleGraph;

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

        @Override
        public String toString() {
            return "Difference{" +
                    "source=" + source +
                    ", dest=" + dest +
                    ", firstDistance=" + firstDistance +
                    ", secondDistance=" + secondDistance +
                    '}';
        }
    }

    public static class PathDifference {
        public final Module source;
        public final Module dest;
        public final List<Module> firstPath;
        public final List<Module> secondPath;

        public PathDifference(Module source, Module dest, List<Module> firstPath, List<Module> secondPath) {
            this.source = source;
            this.dest = dest;
            this.firstPath = firstPath;
            this.secondPath = secondPath;
        }

        @Override
        public String toString() {
            return "PathDifference{" +
                    "source=" + source +
                    ", dest=" + dest +
                    ", firstPath=" + firstPath +
                    ", secondPath=" + secondPath +
                    '}';
        }
    }

    private final List<Module>[][] minimumPathMatrix;
    private final Map<Module,Integer> indexMap;
    private final Collection<Module> modules;

    public ModuleGraphTransitiveClosure(ModuleGraph moduleGraph, Collection<Module> modules) {
        this.modules = modules;
        minimumPathMatrix = initialiseSquareMatrixTo(modules.size());
        indexMap = createMapModuleIndex(modules);

        initialiseDistanceOneModules(modules,moduleGraph);

        applyFloydWarshallMainIteration();
    }

    @SuppressWarnings("unchecked")
    private List<Module>[][] initialiseSquareMatrixTo(final int size) {
        final List<Module>[][] array = (List<Module>[][]) new List[size][size];
        for(int i=0; i < array.length; ++i)
            for(int j=0; j < array.length; ++j)
                array[i][j] = new ArrayList<Module>();
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
                minimumPathMatrix[indexMap.get(module)][indexMap.get(dependency)].add(dependency);
            }
        }
    }

    private void applyFloydWarshallMainIteration() {
        for(int i = 0; i < minimumPathMatrix.length; ++i) {
            for(int j = 0; j < minimumPathMatrix.length; ++j) {
                for(int k = 0; k < minimumPathMatrix.length; ++k) {
                    List<Module> pathIJ = minimumPathMatrix[i][j];
                    List<Module> pathIK = minimumPathMatrix[i][k];
                    List<Module> pathKJ = minimumPathMatrix[k][j];
                    if(pathIJ.isEmpty() && ! pathIK.isEmpty() && !pathKJ.isEmpty()) {
                        minimumPathMatrix[i][j].clear();
                        minimumPathMatrix[i][j].addAll(pathIK);
                        minimumPathMatrix[i][j].addAll(pathKJ);
                    } else if(!pathIJ.isEmpty() && !pathIK.isEmpty() && !pathKJ.isEmpty()) {
                        if(pathIK.size() + pathKJ.size() < pathIJ.size()) {
                            minimumPathMatrix[i][j].clear();
                            minimumPathMatrix[i][j].addAll(pathIK);
                            minimumPathMatrix[i][j].addAll(pathKJ);
                        }
                    }
                }
            }
        }
    }

    public Boolean isReachable(Module vertex1, Module vertex2) {
        return minimumDistance(vertex1,vertex2).map((a) -> a < Integer.MAX_VALUE).orElse(false);
    }

    public boolean same(ModuleGraphTransitiveClosure other) {
        return diff(other).map(List::isEmpty).orElse(false);
    }

    public Optional<List<Difference>> diff(ModuleGraphTransitiveClosure other) {
        return diffPath(other).map((argument) -> {
                final List<Difference> result = new ArrayList<Difference>(argument.size());
                for(PathDifference pathDifference: argument) {
                    result.add(new Difference(pathDifference.source,pathDifference.dest,pathDifference.firstPath.size(),pathDifference.secondPath.size()));
                }
                return result;
            }
        );
    }

    public Optional<List<PathDifference>> diffPath(ModuleGraphTransitiveClosure other) {
        if(!modules.containsAll(other.modules) || !other.modules.containsAll(modules))
            return Optional.empty();
        final List<PathDifference> differences = new ArrayList<PathDifference>();
        for(Module i : modules) {
            for (Module j : modules) {
                int thisI = indexMap.get(i),
                        thisJ = indexMap.get(j),
                        otherI = indexMap.get(i),
                        otherJ = indexMap.get(j);
                if(minimumPathMatrix[thisI][thisJ].size() != other.minimumPathMatrix[otherI][otherJ].size())
                    differences.add(new PathDifference(i,j, minimumPathMatrix[thisI][thisJ],
                            other.minimumPathMatrix[otherI][otherJ]));
            }
        }
        return Optional.of(differences);
    }

    public Optional<Integer> minimumDistance(Module vertex1, Module vertex2) {
        if(indexMap.get(vertex1) == null || indexMap.get(vertex2) == null)
            return Optional.empty();
        else {
            final int distance = minimumPathMatrix[indexMap.get(vertex1)][indexMap.get(vertex2)].size();
            return Optional.of(distance == 0 ? Integer.MAX_VALUE : distance);
        }
    }

    public List<Module> minimumDistancePath(Module vertex1, Module vertex2) {
        if(indexMap.get(vertex1) == null || indexMap.get(vertex2) == null)
            return new ArrayList<Module>();
        else
            return minimumPathMatrix[indexMap.get(vertex1)][indexMap.get(vertex2)];
    }
}
