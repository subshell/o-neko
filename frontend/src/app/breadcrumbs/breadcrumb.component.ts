import {Component, Input} from "@angular/core";
import {Breadcrumb} from "./breadcrumb";

@Component({
  selector: 'breadcrumbs',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent {

  @Input()
  public breadcrumbs: Array<Breadcrumb>;

  constructor() {
  }

}
