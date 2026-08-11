package be.cytomine.dto.meilisearch;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MeiliSearchFacetsResponse {
    @JsonProperty("block.block_preparation.meaning")
    private Map<String, Double> blockBlockPreparationMeaning;

    @JsonProperty("slide.staining.stains.compound.meaning")
    private Map<String, Double> slideStainingStainsCompoundMeaning;

    @JsonProperty("specimens.age_at_extraction.interval_start")
    private Map<String, Double> specimensAgeAtExtractionIntervalStart;

    @JsonProperty("specimens.anatomical_site.meaning")
    private Map<String, Double> specimensAnatomicalSiteMeaning;

    @JsonProperty("specimens.biological_being.animal_species.meaning")
    private Map<String, Double> specimensBiologicalBeingAnimalSpeciesMeaning;

    @JsonProperty("specimens.biological_being.sex")
    private Map<String, Double> specimensBiologicalBeingSex;

    @JsonProperty("specimens.fixation_type.meaning")
    private Map<String, Double> specimensFixationTypeMeaning;

    @JsonProperty("specimens.specimen_type.meaning")
    private Map<String, Double> specimensSpecimenTypeMeaning;
}