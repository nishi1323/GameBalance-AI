package com.gamebalance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StartGamingSessionRequest {

    @NotBlank(message = "Game name is required")
    @Size(max = 100, message = "Game name must be under 100 characters")
    private String gameName;

    @Size(max = 300, message = "Notes must be under 300 characters")
    private String notes;

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
