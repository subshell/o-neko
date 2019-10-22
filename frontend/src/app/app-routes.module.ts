import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DockerRegistryListComponent} from "./docker/list/docker-registry-list.component";
import {HomeComponent} from "./home/home.component";
import {LoginComponent} from "./login/login.component";
import {LogsComponent} from "./logs/logs.component";
import {DefinedNamespacesListComponent} from "./namespace/list/defined-namespaces-list.component";
import {EditProjectVersionComponent} from "./project/edit-version/edit-project-version.component";
import {EditProjectComponent} from "./project/edit/edit-project.component";
import {ProjectListComponent} from "./project/list/project-list.component";
import {ProjectVersionListComponent} from "./project/versions-list/project-version-list.component";
import {EditMeshComponent} from "./projectmesh/edit-mesh/edit-mesh.component";
import {ProjectMeshListComponent} from "./projectmesh/list/project-mesh-list.component";
import {ManageMeshComponentsComponent} from "./projectmesh/manage-components/manage-mesh-components.component";
import {AdminGuardService} from "./session/admin-guard.service";
import {AuthGuardService} from "./session/auth-guard.service";
import {IsAdminOrDoerGuardService} from "./session/is-admin-or-doer-guard.service";
import {UserListComponent} from "./user/list/user-list.component";
import {MeComponent} from "./user/me/me.component";


export interface SupportsSidenav {
  supportsSidenav?: boolean
}

export const APP_ROUTES: Routes = [
  {
    path: '',
    component: HomeComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'logs',
    component: LogsComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: false
    } as SupportsSidenav
  },
  {
    path: 'login',
    component: LoginComponent,
    data: {
      supportsSidenav: false
    } as SupportsSidenav
  },
  {
    path: 'projects',
    component: ProjectListComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'projects/:id',
    component: EditProjectComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'projects/:id/versions',
    component: ProjectVersionListComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'projects/:id/versions/:versionId',
    component: EditProjectVersionComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'project-meshes',
    component: ProjectMeshListComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'project-meshes/:id',
    component: EditMeshComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'project-meshes/:id/components',
    component: ManageMeshComponentsComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'users',
    component: UserListComponent,
    canActivate: [AuthGuardService, AdminGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'docker-registries',
    component: DockerRegistryListComponent,
    canActivate: [AuthGuardService, IsAdminOrDoerGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'namespaces',
    component: DefinedNamespacesListComponent,
    canActivate: [AuthGuardService, IsAdminOrDoerGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: 'me',
    component: MeComponent,
    canActivate: [AuthGuardService],
    data: {
      supportsSidenav: true
    } as SupportsSidenav
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(APP_ROUTES, {
      onSameUrlNavigation: 'ignore',
      useHash: false
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
