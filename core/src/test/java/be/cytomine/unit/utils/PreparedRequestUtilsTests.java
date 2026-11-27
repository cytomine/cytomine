package be.cytomine.unit.utils;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;

import be.cytomine.utils.PreparedRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class PreparedRequestUtilsTests {

    @Test
    public void setUrl() {
        PreparedRequest pr = new PreparedRequest();
        pr.setUrl("http://domain.com");
        assertThat(pr.getScheme()).isEqualTo("http");
        assertThat(pr.getHost()).isEqualTo("domain.com");
        assertThat(pr.getPort()).isEqualTo(-1);
        assertThat(pr.getPath()).isEqualTo("");
        assertThat(pr.getHeaders()).containsKey("Host");
        assertThat(pr.getHeaders()).containsEntry("Host", List.of("domain.com"));

        pr = new PreparedRequest();
        pr.setUrl("https://domain.com:10900/basepath");
        assertThat(pr.getScheme()).isEqualTo("https");
        assertThat(pr.getHost()).isEqualTo("domain.com");
        assertThat(pr.getPort()).isEqualTo(10900);
        assertThat(pr.getPath()).isEqualTo("/basepath");
        assertThat(pr.getHeaders()).containsKey("Host");
        assertThat(pr.getHeaders()).containsEntry("Host", List.of("domain.com"));
    }

    @Test
    public void buildPath() {
        PreparedRequest pr = new PreparedRequest();
        pr.addPathFragment("a");
        pr.addPathFragment("/b/");
        pr.addPathFragment("c/");
        pr.addPathFragment("/d");
        assertThat(pr.getPath()).isEqualTo("/a/b/c/d");

        pr.addPathFragment("/dir/path,&2%3.jpg");
        assertThat(pr.getPath()).isEqualTo("/a/b/c/d/dir/path,&2%3.jpg");
    }

    @Test
    public void buildQuery() {
        PreparedRequest pr = new PreparedRequest();
        pr.addQueryParameter("a", "1");
        pr.addQueryParameter("b", "");
        pr.addQueryParameter("c", "value");
        assertThat(pr.getQuery()).isEqualTo("a=1&c=value");

        pr.addQueryParameter("d", null);
        pr.addQueryParameter("e", 3);
        assertThat(pr.getQuery()).isEqualTo("a=1&c=value&e=3");
    }

    @Test
    public void buildUri() {
        PreparedRequest pr = new PreparedRequest();
        pr.setUrl("https://domain.com:10900/basepath");
        pr.addPathFragment("/b/");
        pr.addPathFragment("c/");
        pr.addQueryParameter("d", null);
        pr.addQueryParameter("e", 3);
        assertThat(pr.getURI().toString()).isEqualTo("https://domain.com:10900/basepath/b/c?e=3");
    }

    @Test
    public void pathFragmentIsEncodedOnce() {
        PreparedRequest pr = new PreparedRequest();
        pr.setUrl("http://pims:5000");
        pr.addPathFragment("image");
        pr.addPathFragment("upload-1/two words [1] café?.ndpi");
        pr.addPathFragment("info");

        URI uri = pr.getURI();

        // getRawPath() is the path as it goes on the wire; getPath() is the same path decoded
        assertThat(uri.getRawPath())
            .isEqualTo("/image/upload-1/two%20words%20%5B1%5D%20caf%C3%A9%3F.ndpi/info");
        assertThat(uri.getPath())
            .isEqualTo("/image/upload-1/two words [1] café?.ndpi/info");

        // nothing non-ASCII is left anywhere in the URI, query included
        assertThat(uri.toString()).isEqualTo(uri.toASCIIString());
    }
}
