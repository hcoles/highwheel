package org.example.core;

import org.example.core.api.IOInterface;
import org.example.core.internals.BusinessLogic1;
import org.example.core.model.Entity1;

public class CoreFacade {

    public CoreFacade(IOInterface ioInterface) {}

    public void facadeMethod1() {
        final BusinessLogic1 bl1 = new BusinessLogic1(new Entity1());
    }
}
