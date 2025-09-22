package br.edu.iff.taskflowapi.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskRequest {

    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String limitDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(String limitDate) {
        this.limitDate = limitDate;
    }
}
