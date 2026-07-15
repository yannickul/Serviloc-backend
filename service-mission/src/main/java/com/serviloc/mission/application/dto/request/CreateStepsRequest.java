// application/dto/request/CreateStepsRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class CreateStepsRequest {

    @NotNull(message = "La durée estimée est obligatoire")
    @Positive(message = "La durée estimée doit être positive")
    private Integer estimatedDurationHours;
    @NotEmpty(message = "Au moins une étape est requise")
    @Valid
    private List<StepInput> steps;

    public List<StepInput> getSteps() { return steps; }
    public void setSteps(List<StepInput> steps) { this.steps = steps; }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public static class StepInput {
        @NotBlank(message = "Le libellé de l'étape est obligatoire")
        private String label;
        private Integer order;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
    }
}