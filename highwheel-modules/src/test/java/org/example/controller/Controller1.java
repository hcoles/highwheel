package org.example.controller;

import org.example.core.CoreFacade;

public class Controller1 {
    public static class ControllerModelClass {

    }

    private final CoreFacade cf;

    public Controller1(CoreFacade cf) {
        this.cf = cf;
    }

    public ControllerModelClass access() {
        cf.facadeMethod1();
        return new ControllerModelClass();
    }
}
