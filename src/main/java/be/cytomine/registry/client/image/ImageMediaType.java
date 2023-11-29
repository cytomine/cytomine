package io.github.ya_b.registry.client.image;

import com.google.gson.annotations.SerializedName;

public enum ImageMediaType {
    @SerializedName("application/vnd.docker.distribution.manifest.v2+json")
    MANIFEST_V2("application/vnd.docker.distribution.manifest.v2+json"),

    @SerializedName("application/vnd.docker.container.image.v1+json")
    MANIFEST_V1("application/vnd.docker.container.image.v1+json"),

    @SerializedName("application/vnd.docker.container.image.v1+json")
    CONFIG("application/vnd.docker.container.image.v1+json"),

    @SerializedName("application/vnd.docker.image.rootfs.diff.tar.gzip")
    LAYER("application/vnd.docker.image.rootfs.diff.tar.gzip"),

    @SerializedName("application/vnd.docker.image.rootfs.foreign.diff.tar.gzip")
    LAYER_NO_PUSH("application/vnd.docker.image.rootfs.foreign.diff.tar.gzip"),

    @SerializedName("application/vnd.oci.image.manifest.v1+json")
    MANIFEST_OCI("application/vnd.oci.image.manifest.v1+json"),

    @SerializedName("application/vnd.oci.image.config.v1+json")
    CONFIG_OCI("application/vnd.oci.image.config.v1+json"),

    @SerializedName("application/vnd.oci.image.layer.v1.tar+gzip")
    LAYER_OCI("application/vnd.oci.image.layer.v1.tar+gzip");



    private String value;

    ImageMediaType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}