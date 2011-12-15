package com.lopuhin.smartsketcher;


public interface IAction {
    void doAction(Sheet sheet);
    void undoAction(Sheet sheet);
}
