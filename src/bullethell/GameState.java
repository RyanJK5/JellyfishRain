package bullethell;

public enum GameState {
    DEFAULT, MENU, ENCOUNTER, BOSS, CUTSCENE;

    public boolean screenLocked() {
        switch (this) {
            case BOSS:
                return false;
            case CUTSCENE:
                return true;
            case DEFAULT:
                return false;
            case ENCOUNTER:
                return false;
            case MENU:
                return true;
        }
        return false;
    }

    public boolean combat() {
        return this == BOSS || this == ENCOUNTER;
    }
}