package be.cytomine.domain.appengine;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.utils.JsonObject;

@ToString
@NoArgsConstructor
@Setter
@Getter
@Entity
public class TaskRunLayer extends CytomineDomain {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "layer_id")
    private AnnotationLayer annotationLayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_run_id")
    private TaskRun taskRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_instance_id")
    private ImageInstance image;

    @Column(name = "parameter_name")
    private String parameterName;

    @Column(name = "derived_from")
    private String derivedFrom;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "task_run_layer_id", nullable = false)
    @OrderColumn(name = "order_index")
    private List<CropOffset> offsets = new ArrayList<>();

    public TaskRunLayer(AnnotationLayer layer, TaskRun taskRun, ImageInstance image, String parameterName) {
        this.annotationLayer = layer;
        this.taskRun = taskRun;
        this.image = image;
        this.parameterName = parameterName;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        TaskRunLayer taskRunLayer = (TaskRunLayer) domain;
        JsonObject domainData = CytomineDomain.getDataFromDomain(domain);
        domainData.put("annotationLayer", taskRunLayer.getAnnotationLayer().getId());
        domainData.put("taskRun", taskRunLayer.getTaskRun().getId());
        domainData.put("image", taskRunLayer.getImage().getId());

        return domainData;
    }

    @Override
    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        TaskRunLayer taskRunLayer = this;
        taskRunLayer.id = json.getJSONAttrLong("id", null);
        taskRunLayer.annotationLayer = (AnnotationLayer) json.getJSONAttrDomain(
            entityManager, "annotationLayer", new AnnotationLayer(), true
        );
        taskRunLayer.taskRun = (TaskRun) json.getJSONAttrDomain(entityManager, "taskRun", new TaskRun(), true);
        taskRunLayer.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        taskRunLayer.created = json.getJSONAttrDate("created");
        taskRunLayer.updated = json.getJSONAttrDate("updated");

        return taskRunLayer;
    }

    @Override
    public CytomineDomain container() {
        return this;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }
}
