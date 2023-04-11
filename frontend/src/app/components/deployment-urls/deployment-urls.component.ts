import {Component, Input} from "@angular/core";
import {ProjectVersion} from "../../project/project-version";

@Component({
  selector: 'on-deployment-urls',
  templateUrl: './deployment-urls.component.html',
  styleUrls: ['./deployment-urls.component.scss']
})
export class DeploymentUrlsComponent {

  @Input() version: ProjectVersion;

}
