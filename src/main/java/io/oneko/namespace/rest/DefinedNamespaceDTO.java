package io.oneko.namespace.rest;

import java.util.UUID;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class DefinedNamespaceDTO {

	private UUID id;
	private String name;
}
