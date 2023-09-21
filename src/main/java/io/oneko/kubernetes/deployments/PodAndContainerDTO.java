package io.oneko.kubernetes.deployments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodAndContainerDTO {

	public static PodAndContainerDTO build(String podName, List<String> containerNames, Function<String, String> containerExternalLogUrlMapper) {
		List<Container> containers = containerNames.stream().map(c -> new Container(c, containerExternalLogUrlMapper.apply(c))).toList();
		return new PodAndContainerDTO(podName, containers);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Container {
		private String name;
		private String externalLogUrl;
	}

	private String podName;
	private List<Container> containers;
}
