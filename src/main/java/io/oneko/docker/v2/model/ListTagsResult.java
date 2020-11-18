package io.oneko.docker.v2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTagsResult {
	private String name;
	private List<String> tags;
}
