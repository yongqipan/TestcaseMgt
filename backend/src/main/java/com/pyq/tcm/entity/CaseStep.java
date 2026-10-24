package com.pyq.tcm.entity;

public class CaseStep {

    private String action;
    private String expected;

    public CaseStep() {
    }

    public CaseStep(String action, String expected) {
        this.action = action;
        this.expected = expected;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }
}
