package be.cytomine.unit.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import be.cytomine.utils.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTests {

    @Test
    public void pagination_from_already_limited_results() {
        assertThat(StringUtils.obscurify("password", 0)).isEqualTo("********");
        assertThat(StringUtils.obscurify("password", 1)).isEqualTo("p******d");
        assertThat(StringUtils.obscurify("password", 2)).isEqualTo("pa****rd");
        assertThat(StringUtils.obscurify("password", 3)).isEqualTo("pas**ord");
        assertThat(StringUtils.obscurify("password", 4)).isEqualTo("password");
        assertThat(StringUtils.obscurify("password", 5)).isEqualTo("password");
        assertThat(StringUtils.obscurify("password", 10)).isEqualTo("password");

        assertThat(StringUtils.obscurify("", 0)).isEqualTo("<EMPTY>");
        assertThat(StringUtils.obscurify("", 10)).isEqualTo("<EMPTY>");
    }

    @Test
    public void null_or_empty_parameters_extracted_as_null() {
        assertThat(StringUtils.extractListFromParameter("")).isNull();
        assertThat(StringUtils.extractListFromParameter(null)).isNull();
    }

    @Test
    public void parameters_extracted_as_list_of_long() {
        List<Long> expectedList = new ArrayList<>(Arrays.asList((long)145, (long)146, (long)0, (long)-1));
        assertThat(StringUtils.extractListFromParameter("145,146,0,-1")).isEqualTo(expectedList);
    }

    @Test
    public void get_local_date_as_string() {
        List<Long> expectedList = new ArrayList<>(Arrays.asList((long)145, (long)146, (long)0, (long)-1));
        assertThat(StringUtils.extractListFromParameter("145,146,0,-1")).isEqualTo(expectedList);
    }

    @Test
    public void get_simple_format_local_date_as_string() {
        List<Long> expectedList = new ArrayList<>(Arrays.asList((long)145, (long)146, (long)0, (long)-1));
        assertThat(StringUtils.extractListFromParameter("145,146,0,-1")).isEqualTo(expectedList);
    }
}
