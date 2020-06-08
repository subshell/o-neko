import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {LoginComponent} from "./views/login/login.component";
import {AuthGuardService} from "./session/auth-guard.service";
import {MainComponent} from "./views/main/main.component";
import {DeploymentsDashboardComponent} from "./deployable/deployments-dashboard/deployments-dashboard.component";
import {ProjectListComponent} from "./project/list/project-list.component";
import {UserListComponent} from "./user/list/user-list.component";
import {AdminGuardService} from "./session/admin-guard.service";
import {EditProjectComponent} from "./project/edit/edit-project.component";
import {ProjectVersionListComponent} from "./project/versions-list/project-version-list.component";
import {EditProjectVersionComponent} from "./project/edit-version/edit-project-version.component";
import {ProjectMeshListComponent} from "./projectmesh/list/project-mesh-list.component";
import {ManageMeshComponentsComponent} from "./projectmesh/manage-components/manage-mesh-components.component";
import {EditMeshComponent} from "./projectmesh/edit-mesh/edit-mesh.component";
import {DockerRegistryListComponent} from "./docker/list/docker-registry-list.component";
import {IsAdminOrDoerGuardService} from "./session/is-admin-or-doer-guard.service";
import {DefinedNamespacesListComponent} from "./namespace/list/defined-namespaces-list.component";
import {MeComponent} from "./user/me/me.component";
import {LogsComponent} from "./logs/logs.component";

export const APP_ROUTES: Routes = [
  // Login
  {
    path: 'login',
    component: LoginComponent
  },

  // The main stuff begins here:
  {
    path: '',
    canActivate: [AuthGuardService],
    component: MainComponent,
    children: [
      {
        path: '',
        component: DeploymentsDashboardComponent
      },
      {
        path: 'projects',
        children: [
          {
            path: ':id',
            children: [
              {
                path: 'versions',
                children: [
                  {
                    path: ':versionId',
                    component: EditProjectVersionComponent
                  },
                  {
                    path: '',
                    component: ProjectVersionListComponent,
                  }
                ]
              },
              {
                path: '',
                component: EditProjectComponent
              }
            ]
          },
          {
            path: '',
            component: ProjectListComponent,
          }
        ]
      },
      {
        path: 'project-meshes',
        children: [
          {
            path: ':id',
            children: [
              {
                path: 'components',
                component: ManageMeshComponentsComponent,
              },
              {
                path: '',
                component: EditMeshComponent,
              }
            ]
          },
          {
            path: '',
            component: ProjectMeshListComponent
          }
        ]
      },
      {
        path: 'users',
        component: UserListComponent,
        canActivate: [AuthGuardService, AdminGuardService]
      },
      {
        path: 'docker-registries',
        component: DockerRegistryListComponent,
        canActivate: [AuthGuardService, IsAdminOrDoerGuardService]
      },
      {
        path: 'namespaces',
        component: DefinedNamespacesListComponent,
        canActivate: [AuthGuardService, IsAdminOrDoerGuardService]
      },
      {
        path: 'me',
        component: MeComponent,
        canActivate: [AuthGuardService]
      },
      {
        path: 'logs',
        component: LogsComponent,
        canActivate: [AuthGuardService]
      },
      {
        path: '**',
        redirectTo: ''
      }
    ]
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
