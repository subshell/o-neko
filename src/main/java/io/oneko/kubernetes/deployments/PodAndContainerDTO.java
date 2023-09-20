package io.oneko.kubernetes.deployments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodAndContainerDTO {
	private String podName;
	private List<String> containerNames;
}
