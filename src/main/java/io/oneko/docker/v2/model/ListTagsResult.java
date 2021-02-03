package io.oneko.docker.v2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTagsResult {
	private String name;
	private List<String> tags;
}
