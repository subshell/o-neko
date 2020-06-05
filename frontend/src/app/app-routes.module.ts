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
        component: ProjectListComponent,
        children: [
          {
            path: ':id',
            component: EditProjectComponent,
          },
        ]
      },
      {
        path: 'users',
        component: UserListComponent,
        canActivate: [AdminGuardService]
      },
    ]
  },










  // OLD:
  /*
  {
    path: 'logs',
    component: LogsComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'projects',
    component: ProjectListComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'projects/:id',
    component: EditProjectComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'projects/:id/versions',
    component: ProjectVersionListComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'projects/:id/versions/:versionId',
    component: EditProjectVersionComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'project-meshes',
    component: ProjectMeshListComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'project-meshes/:id',
    component: EditMeshComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'project-meshes/:id/components',
    component: ManageMeshComponentsComponent,
    canActivate: [AuthGuardService]
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
  },*/
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
