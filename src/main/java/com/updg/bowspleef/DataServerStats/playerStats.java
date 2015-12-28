package com.updg.bowspleef.DataServerStats;

import java.util.List;

/**
 * Created by Alex
 * Date: 12.11.13  21:17
 */
public class playerStats {
    private int _playerId;
    private long _timeInGame;
    private boolean _isWinner;
    private int _shots;
    private int _position;

    public int getPlayerId() {
        return _playerId;
    }

    public void setPlayerId(int _playerId) {
        this._playerId = _playerId;
    }

    public long getTimeInGame() {
        return _timeInGame;
    }

    public void setTimeInGame(long _timeInGame) {
        this._timeInGame = _timeInGame;
    }

    public boolean isIsWinner() {
        return _isWinner;
    }

    public void setIsWinner(boolean _isWinner) {
        this._isWinner = _isWinner;
    }

    public int getShots() {
        return _shots;
    }

    public void setShots(int _shots) {
        this._shots = _shots;
    }

    public int getPosition() {
        return _position;
    }

    public void setPosition(int _position) {
        this._position = _position;
    }
}
