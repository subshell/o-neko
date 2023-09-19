package io.oneko.docker.v2.model.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerRegistryManifest {
    @Data
    static class Config {
        String digest;
        String mediaType;
        int size;
    }

    @Data
    static class Platform {
        String architecture;
        String os;
    }

    @Data
    public static class Manifest {
        String digest;
        String mediaType;
        Platform platform;
        int size;

        public Digest getDigest() {
            return new Digest(digest);
        }
    }

    @Data
    public static class Digest {
        String algorithm;
        String digest;

        public Digest(String digest) {
            final String[] split = StringUtils.defaultString(digest).split(":");
            this.algorithm = StringUtils.defaultString(split[0]);
            this.digest = StringUtils.defaultString(split[1]);
        }

        public String getFullDigest() {
            return algorithm + ":" + digest;
        }
    }


    private String mediaType;
    private Config config;
    private List<Manifest> manifests;


    public Digest getDigest() {
        if (!isManifestList()) {
            return new Digest(config.digest);
        }
        throw new IllegalStateException("unsupported mediaType: " + mediaType);
    }

    public boolean isManifestList() {
        return manifests != null && !manifests.isEmpty();
    }
}
