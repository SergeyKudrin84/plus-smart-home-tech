package analyzer.repository;

import analyzer.model.ScenarioAction;
import analyzer.model.ScenarioActionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {

    List<ScenarioAction> findByScenarioId(Long scenarioId);
}