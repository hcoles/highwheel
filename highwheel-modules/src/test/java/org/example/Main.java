package org.example;

import org.example.controller.Controller1;
import org.example.core.CoreFacade;
import org.example.core.api.IOInterface;
import org.example.io.IOImplementaion;

public class Main {
    public static void main(String[] argv) {
        IOInterface ioInterface = new IOImplementaion();
        CoreFacade coreFacade = new CoreFacade(ioInterface);
        Controller1 controller1 = new Controller1(coreFacade);
        controller1.access();
    }
}
