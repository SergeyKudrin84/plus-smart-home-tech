package analyzer.repository;

import analyzer.model.ScenarioCondition;
import analyzer.model.ScenarioConditionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {

    List<ScenarioCondition> findByScenarioId(Long scenarioId);
}