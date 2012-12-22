package com.google.code.corewars8086.war;

import java.util.EventListener;

/**
 * @author BS
 */
public interface ScoreEventListener extends EventListener {
    void scoreChanged(String name, float addedValue, int groupIndex, int subIndex);
}
