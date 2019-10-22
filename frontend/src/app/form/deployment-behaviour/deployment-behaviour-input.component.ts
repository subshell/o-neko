import {Component, Input} from "@angular/core";
import {DeploymentBehaviour} from "../../project/project";

export type HasDeploymentBehaviour = { deploymentBehaviour: DeploymentBehaviour };

@Component({
  selector: 'deployment-behaviour-input',
  templateUrl: './deployment-behaviour-input.component.html',
  styleUrls: ['./deployment-behaviour-input.component.scss']
})
export class DeploymentBehaviourInputComponent {

  @Input() model: HasDeploymentBehaviour;
  @Input() required?: boolean = false;
  @Input() readonly?: boolean = false;

  constructor() {
  }

}
