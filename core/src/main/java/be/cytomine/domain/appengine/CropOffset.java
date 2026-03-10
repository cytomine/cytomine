package be.cytomine.domain.appengine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CropOffset extends CytomineDomain {
    private int x;
    private int y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_run_layer_id", nullable = false)
    private TaskRunLayer taskRunLayer;

    @Column(name = "order_index")
    private int order;

    @Override
    public JsonObject toJsonObject() {
        return null;
    }
}
