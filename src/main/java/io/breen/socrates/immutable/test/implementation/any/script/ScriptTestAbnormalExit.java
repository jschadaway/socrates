package io.breen.socrates.immutable.test.implementation.any.script;

public class ScriptTestAbnormalExit extends Exception {
    public ScriptTestAbnormalExit(ScriptTest test, int exitCode) {
        super("ScriptTest " + test + " exited with code " + exitCode);
    }
}