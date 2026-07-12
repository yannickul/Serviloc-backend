// application/dto/request/CreateStepsRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateStepsRequest {

    @NotEmpty(message = "Au moins une étape est requise")
    @Valid
    private List<StepInput> steps;

    public List<StepInput> getSteps() { return steps; }
    public void setSteps(List<StepInput> steps) { this.steps = steps; }

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