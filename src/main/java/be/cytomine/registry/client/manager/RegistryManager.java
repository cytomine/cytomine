package io.github.ya_b.registry.client.manager;

import io.github.ya_b.registry.client.constant.Constants;
import io.github.ya_b.registry.client.constant.FileConstant;
import io.github.ya_b.registry.client.exception.RegistryException;
import io.github.ya_b.registry.client.http.RegistryApi;
import io.github.ya_b.registry.client.http.resp.CatalogResp;
import io.github.ya_b.registry.client.image.Blob;
import io.github.ya_b.registry.client.image.Context;
import io.github.ya_b.registry.client.image.registry.ManifestHttp;
import io.github.ya_b.registry.client.name.Reference;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class RegistryManager {

    private final RegistryApi api = new RegistryApi();

    public void load(Context context, Reference reference) throws IOException {
        context.setReference(reference);
        Optional<ManifestHttp> manifestHttp = api.getManifest(reference, context.getToken());
        if (!manifestHttp.isPresent()) {
            throw new RegistryException("not support");
        }
        ManifestHttp.BlobDTO configInfo = manifestHttp.get().getConfig();
        String configName = configInfo.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_JSON;
        context.setConfig(new Blob(configName, configInfo.getSize(), configInfo.getDigest(),
                () -> api.getBlob(reference, configInfo.getDigest(), context.getToken())));
        List<Blob> layers = new ArrayList<>();
        for (ManifestHttp.BlobDTO blob : manifestHttp.get().getLayers()) {
            String layerName = blob.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_TAR_GZ;
            layers.add(new Blob(layerName, blob.getSize(), blob.getDigest(), () -> api.getBlob(reference, blob.getDigest(), context.getToken())));
        }
        context.setLayers(layers);
    }

    public void push(Context context, Reference reference) throws IOException {
        List<Blob> blobList = new ArrayList<>(context.getLayers());
        blobList.add(context.getConfig());
        for (Blob blob : blobList) {
            if (!api.isBlobExists(reference, blob.getDigest(), context.getToken())) {
                String uploadUrl = api.startPush(reference, context.getToken());
                try (InputStream is = blob.getContent().get()) {
                    api.uploadBlob(uploadUrl, blob.getDigest(), is, blob.getSize(), context.getToken());
                }
                if (!api.isBlobExists(reference, blob.getDigest(), context.getToken())) {
                    throw new RegistryException("upload blob failed");
                }
            }
        }
        ManifestHttp manifestHttp = context.manifestHttp();
        api.uploadManifest(reference, manifestHttp, manifestHttp.getMediaType(), context.getToken());
    }

    public Optional<String> digest(Context context, Reference reference) throws IOException {
        return api.digest(reference, context.getToken());
    }

    public List<String> tags(Context context, Reference reference) throws IOException {
        return api.tags(reference, context.getToken());
    }

    public void delete(Context context, Reference reference) throws IOException {
        api.deleteManifest(reference, reference.getDigest(), context.getToken());
    }

    public void copy(Context context, String dst) throws IOException {
        Reference dstReference = Reference.parse(dst);
        if (!Objects.equals(context.getReference().getEndpoint(), dstReference.getEndpoint())) {
            push(context, dstReference);
            return;
        }
        List<Blob> blobList = new ArrayList<>(context.getLayers());
        blobList.add(context.getConfig());
        for (Blob blob : blobList) {
            if (!api.isBlobExists(dstReference, blob.getDigest(), context.getToken())) {
                Optional<String> uploadUrl = api.mountBlob(dstReference, blob.getDigest(), context.getReference().getName(), context.getToken());
                if (uploadUrl.isPresent()) {
                    try (InputStream is = blob.getContent().get()) {
                        api.uploadBlob(uploadUrl.get(), blob.getDigest(), is, blob.getSize(), context.getToken());
                    }
                }
                if (!api.isBlobExists(dstReference, blob.getDigest(), context.getToken())) {
                    throw new RegistryException("upload blob failed");
                }
            }
        }
        ManifestHttp manifestHttp = context.manifestHttp();
        api.uploadManifest(dstReference, manifestHttp, manifestHttp.getMediaType(), context.getToken());
    }

    public CatalogResp catalog(Context context, Integer count, String last) throws IOException {
        return api.catalog(context.getReference(), count, last, context.getToken());
    }

    public String getSchema(String endpoint) {
        try {
            api.base(String.format("%s://%s", Constants.SCHEMA_HTTPS, endpoint));
            return Constants.SCHEMA_HTTPS;
        } catch (SSLException e) {
            log.trace("getSchema", e);
        } catch (IOException e) {
            log.warn("getSchema", e);
            throw new RuntimeException("No response from the registry Server.");
        }
        try {
            api.base(String.format("%s://%s", Constants.SCHEMA_HTTP, endpoint));
            return Constants.SCHEMA_HTTP;
        } catch (IOException e) {
            log.warn("getSchema", e);
        }
        throw new RuntimeException("No response from the registry Server.");
    }
}
