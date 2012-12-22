package com.google.code.corewars8086.war;

import java.util.ArrayList;
import java.util.List;

public class WarriorGroup {
    private String name;
    private int numberOfWarriors;
    private ArrayList<WarriorData> warriorData;
    public ArrayList<Warrior> warriors;
    private List<Float> scores;
    private float groupScore;

    public WarriorGroup(String name) {
        this.name = name;
        numberOfWarriors = 0;
        warriorData = new ArrayList<WarriorData>();
        scores = new ArrayList<Float>();
    }

    public void addWarrior(WarriorData data) {
        warriorData.add(data);
        numberOfWarriors++;
        scores.add(new Float(0));
    }

    public List<WarriorData> getWarriors() {
        return warriorData;
    }

    public List<Float> getScores() {
        return scores;
    }

    public String getName() {
        return name;
    }

    public float getGroupScore() {
        return groupScore;
    }

    public int addScoreToWarrior(String name, float value) {
        // find this warrior
        int i;
        for (i = 0; i < warriorData.size(); i++) {
            if (warriorData.get(i).getName().equals(name)) {
                scores.set(i, scores.get(i) + value);
                break;
            }
        }
        groupScore += value;
        return i;
    }
}