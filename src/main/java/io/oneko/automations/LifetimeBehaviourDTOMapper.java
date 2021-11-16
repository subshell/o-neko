package io.oneko.automations;

import org.springframework.stereotype.Component;

@Component
public class LifetimeBehaviourDTOMapper {

	public LifetimeBehaviourDTO toLifetimeBehaviourDTO(LifetimeBehaviour behaviour) {
		LifetimeBehaviourDTO behaviourDTO = new LifetimeBehaviourDTO();
		behaviourDTO.setValue(behaviour.getValue());
		behaviourDTO.setType(behaviour.getType());
		return behaviourDTO;
	}

	public LifetimeBehaviour toLifetimeBehaviour(LifetimeBehaviourDTO dto) {
		return dto == null ? null : new LifetimeBehaviour(dto.getType(), dto.getValue());
	}
}
