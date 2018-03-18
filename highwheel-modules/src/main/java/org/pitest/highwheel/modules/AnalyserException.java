package org.pitest.highwheel.modules;

public class AnalyserException extends RuntimeException {
    public AnalyserException(String msg){super(msg);}
    public AnalyserException(Exception cause){super(cause);}
}
