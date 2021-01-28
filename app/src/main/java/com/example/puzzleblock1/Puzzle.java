package com.example.puzzleblock1;

public class Puzzle {

    private int puzzleID;

    private String puzzleName;

    private String puzzleType;

    private String puzzleBody;

    private String puzzleAns;

    public Puzzle(int puzzleID, String puzzleName, String puzzleType, String puzzleBody, String puzzleAns)
    {
        this.puzzleID = puzzleID;
        this.puzzleName = puzzleName;
        this.puzzleType = puzzleType;
        this.puzzleBody = puzzleBody;
        this.puzzleAns = puzzleAns;
    }

    public int getPuzzleID() {
        return puzzleID;
    }

    public void setPuzzleID(int puzzleID) {
        this.puzzleID = puzzleID;
    }

    public String getPuzzleName() {
        return puzzleName;
    }

    public void setPuzzleName(String puzzleName) {
        this.puzzleName = puzzleName;
    }

    public String getPuzzleType() {
        return puzzleType;
    }

    public void setPuzzleType(String puzzleType) {
        this.puzzleType = puzzleType;
    }

    public String getPuzzleBody() {
        return puzzleBody;
    }

    public void setPuzzleBody(String puzzleBody) {
        this.puzzleBody = puzzleBody;
    }

    public String getPuzzleAns() {
        return puzzleAns;
    }

    public void setPuzzleAns(String puzzleAns) {
        this.puzzleAns = puzzleAns;
    }
}
