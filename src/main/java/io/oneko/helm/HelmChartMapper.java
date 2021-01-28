package io.oneko.helm;

import org.mapstruct.Mapper;

import io.oneko.helmapi.model.Chart;

@Mapper(componentModel = "spring")
public abstract class HelmChartMapper {
	public abstract HelmChartDTO toHelmChartDTO(Chart chart);
}
