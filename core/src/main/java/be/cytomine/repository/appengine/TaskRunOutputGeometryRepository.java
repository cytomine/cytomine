package be.cytomine.repository.appengine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.cytomine.domain.appengine.TaskRunOutputGeometry;

public interface TaskRunOutputGeometryRepository extends JpaRepository<TaskRunOutputGeometry, Long> {
    List<TaskRunOutputGeometry> findAllByTaskRunId(Long taskRunId);
}
