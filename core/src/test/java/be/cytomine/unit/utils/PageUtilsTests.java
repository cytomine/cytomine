package be.cytomine.unit.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import be.cytomine.utils.PageUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class PageUtilsTests {

    @Test
    public void pagination_from_already_limited_results() {
        Page page = PageUtils.buildPageFromPageResults(new ArrayList<>(List.of("a", "b", "c", "d", "e")), 0L, 0L, 5L);
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(5);

        page = PageUtils.buildPageFromPageResults(new ArrayList<>(List.of("a", "b", "c")), 3L, 0L, 5L);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);

        page = PageUtils.buildPageFromPageResults(new ArrayList<>(List.of("c", "d", "e")), 3L, 2L, 5L);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);

        page = PageUtils.buildPageFromPageResults(new ArrayList<>(List.of()), 3L, 6L, 5L);
        assertThat(page.getContent()).hasSize(0);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }
}
