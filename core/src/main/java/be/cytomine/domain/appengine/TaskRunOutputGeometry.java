package be.cytomine.domain.appengine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.utils.JsonObject;

@Setter
@Getter
@Entity
public class TaskRunOutputGeometry extends CytomineDomain {

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_run_id", nullable = false)
    private TaskRun taskRun;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_instance_id", nullable = false)
    private ImageInstance image;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject data = CytomineDomain.getDataFromDomain(domain);
        TaskRunOutputGeometry output = (TaskRunOutputGeometry) domain;
        data.put("name", output.getName());
        data.put("taskRun", output.getTaskRun().getTaskRunId().toString());
        data.put("image", output.getImage().getId());
        return data;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }
}
