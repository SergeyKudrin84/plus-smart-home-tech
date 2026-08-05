package model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {

    @NotBlank
    private String name;

    @Override
    @NotNull
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}
