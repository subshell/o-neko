package io.oneko.namespace.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class NamespaceDTO {
	private String name;
	private UUID id;//optional
}
