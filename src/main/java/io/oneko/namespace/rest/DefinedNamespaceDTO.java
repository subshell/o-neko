package io.oneko.namespace.rest;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Data
public class DefinedNamespaceDTO {

	private UUID id;
	private String name;
}
