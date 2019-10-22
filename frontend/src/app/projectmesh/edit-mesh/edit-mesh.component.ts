import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {switchMap} from "rxjs/operators";
import {DefinedNamespace} from "../../namespace/defined-namespace";
import {Namespace} from "../../namespace/namespace";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {ProjectMesh} from "../project-mesh";
import {ProjectMeshService} from "../project-mesh.service";

@Component({
  selector: 'edit-mesh',
  templateUrl: './edit-mesh.component.html'
})
export class EditMeshComponent implements OnInit {

  public projectMesh: ProjectMesh;
  public namespaces: Array<DefinedNamespace> = [];
  public lifetimeBehaviourOptions: Array<{ label: string, value: number }> = [
    {
      label: '1 Day',
      value: 1
    },
    {
      label: '1 Week',
      value: 7
    },
    {
      label: '2 Weeks',
      value: 14
    },
    {
      label: '30 Days',
      value: 30
    },
    {
      label: 'Infinite',
      value: 0
    },
  ];
  private editingUser: User;

  constructor(private rest: RestService,
              private projectMeshService: ProjectMeshService,
              private userService: UserService,
              private route: ActivatedRoute,
              private router: Router) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.namespace().getAllDefinedNamespaces().subscribe(namespaces => this.namespaces = namespaces);
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => this.rest.projectMesh().getProjectMeshById(params.get('id')))
    ).subscribe(mesh => {
      this.projectMesh = mesh;
      if (!this.projectMesh.lifetimeBehaviour) {
        this.projectMesh.lifetimeBehaviour = {daysToLive: undefined};
      }
    });
  }

  public mayEditMeshes(): boolean {
    return this.projectMeshService.isUserAllowedToEditProjectMeshes(this.editingUser);
  }

  public save() {
    this.projectMeshService.saveProjectMesh(this.projectMesh, this.editingUser).subscribe(m => this.projectMesh = m);
  }

  public delete() {
    this.projectMeshService.deleteProjectMesh(this.projectMesh, this.editingUser).subscribe(() => this.router.navigateByUrl('/project-meshes'))
  }

  public compareNameSpaces(o1: DefinedNamespace | Namespace, o2: DefinedNamespace | Namespace): boolean {
    return o1 && o2 ? o1.name === o2.name : o1 === o2;
  }

}
