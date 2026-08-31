package be.cytomine.dto.meilisearch;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MeiliSearchFacetsResponse {
    @JsonProperty("block.block_preparation.meaning")
    private Map<String, Double> blockBlockPreparationMeaning = new HashMap<>();

    @JsonProperty("slide.staining.stains.compound.meaning")
    private Map<String, Double> slideStainingStainsCompoundMeaning = new HashMap<>();

    @JsonProperty("specimens.age_at_extraction.interval_start")
    private Map<String, Double> specimensAgeAtExtractionIntervalStart = new HashMap<>();

    @JsonProperty("specimens.anatomical_site.meaning")
    private Map<String, Double> specimensAnatomicalSiteMeaning = new HashMap<>();

    @JsonProperty("specimens.biological_being.animal_species.meaning")
    private Map<String, Double> specimensBiologicalBeingAnimalSpeciesMeaning = new HashMap<>();

    @JsonProperty("specimens.biological_being.sex")
    private Map<String, Double> specimensBiologicalBeingSex = new HashMap<>();

    @JsonProperty("specimens.fixation_type.meaning")
    private Map<String, Double> specimensFixationTypeMeaning = new HashMap<>();

    @JsonProperty("specimens.specimen_type.meaning")
    private Map<String, Double> specimensSpecimenTypeMeaning = new HashMap<>();
}