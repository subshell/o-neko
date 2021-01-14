package io.oneko.helmapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Repository {
	private String name;
	private String url;
}
