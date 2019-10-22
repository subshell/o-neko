package io.oneko.namespace.rest;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NamespaceDTO {
	private String name;
	private UUID id;//optional
}
