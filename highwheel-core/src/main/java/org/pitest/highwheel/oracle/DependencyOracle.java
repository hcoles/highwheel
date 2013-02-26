package org.pitest.highwheel.oracle;

import org.pitest.highwheel.model.Access;

public interface DependencyOracle {

  DependendencyStatus assess(Access a);

}
