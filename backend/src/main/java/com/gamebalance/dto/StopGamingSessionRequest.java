package com.gamebalance.dto;

import jakarta.validation.constraints.Size;

public class StopGamingSessionRequest {

    @Size(max = 300, message = "Notes must be under 300 characters")
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
