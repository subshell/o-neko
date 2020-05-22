import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {NgModule} from '@angular/core';
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckboxModule, MatRadioModule} from "@angular/material";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatChipsModule} from "@angular/material/chips";
import {MatRippleModule} from "@angular/material/core";
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule, MatIconRegistry} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSelectModule} from "@angular/material/select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSortModule} from "@angular/material/sort";
import {MatStepperModule} from "@angular/material/stepper";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MAT_TOOLTIP_DEFAULT_OPTIONS, MatTooltipDefaultOptions, MatTooltipModule} from "@angular/material/tooltip";
import {BrowserModule, DomSanitizer} from '@angular/platform-browser';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AceEditorModule} from 'ng2-ace-editor';
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {ActivityLogComponent} from "./activity/activity-log/activity-log.component";
import {AppRoutingModule} from "./app-routes.module";

import {AppComponent} from './app.component';
import {BreadcrumbComponent} from "./breadcrumbs/breadcrumb.component";
import {DeployableActionsComponent} from "./deployable/deployable-actions/deployable-actions.component";
import {DeployableStatusComponent} from "./deployable/deployable-status/deployable-status.component";
import {DeploymentsDashboardComponent} from "./deployable/deployments-dashboard/deployments-dashboard.component";
import {ShowDeployableConfigurationDialog} from "./deployable/show-deployable-configuration-dialog/show-deployable-configuration-dialog.component";
import {TemplateEditorComponent} from "./deployable/template-editor/template-editor.component";
import {ConfirmDeletionDialogComponent} from "./docker/confirm-deletion.dialog/confirm-deletion-dialog.component";
import {DockerRegistryEditDialogComponent} from "./docker/edit-dialog/docker-registry-edit-dialog.component";
import {DockerRegistryListComponent} from "./docker/list/docker-registry-list.component";
import {DeploymentBehaviourInputComponent} from "./form/deployment-behaviour/deployment-behaviour-input.component";
import {KeyValueInputComponent} from "./form/key-value-input/key-value-input.component";
import {LifetimeBehaviourInputComponent} from "./form/lifetime-behaviour/lifetime-behaviour-input.component";
import {FileUploadComponent} from "./form/upload/file-upload.component";
import {ValueInputComponent} from './form/value-input/value-input.component';
import {HomeComponent} from "./home/home.component";
import {LoginComponent} from "./login/login.component";
import {LogsComponent} from "./logs/logs.component";
import {CreateNamespaceDialogComponent} from "./namespace/create-namespace-dialog/create-namespace-dialog.component";
import {DeleteNamespaceDialogComponent} from "./namespace/delete-namespace-dialog/delete-namespace-dialog.component";
import {DefinedNamespacesListComponent} from "./namespace/list/defined-namespaces-list.component";
import {NavigationComponent} from "./navigation/navigation.component";
import {CreateProjectDialogComponent} from "./project/create-project-dialog/create-project-dialog.component";
import {ProjectDashboardComponent} from "./project/dashboard/project-dashboard.component";
import {EditProjectVersionComponent} from "./project/edit-version/edit-project-version.component";
import {EditProjectComponent} from "./project/edit/edit-project.component";
import {ProjectListComponent} from "./project/list/project-list.component";
import {ProjectVersionActionsComponent} from "./project/project-version-actions/project-version-actions.component";
import {ProjectVersionVariableActionsComponent} from './project/project-version-variable-actions/project-version-variable-actions.component';
import {ProjectService} from "./project/project.service";
import {TemplateVariableEditorComponent} from './project/template-variables/template-variable-editor/template-variable-editor.component';
import {TemplateVariablesComponent} from './project/template-variables/template-variables.component';
import {ProjectVersionListComponent} from "./project/versions-list/project-version-list.component";
import {CreateMeshComponentDialogComponent} from "./projectmesh/create-mesh-component-dialog/create-mesh-component-dialog.component";
import {CreateProjectMeshDialogComponent} from "./projectmesh/create-mesh-dialog/create-project-mesh-dialog.component";
import {ProjectmeshDashboardComponent} from "./projectmesh/dashboard/projectmesh-dashboard.component";
import {EditMeshComponent} from "./projectmesh/edit-mesh/edit-mesh.component";
import {ProjectMeshListComponent} from "./projectmesh/list/project-mesh-list.component";
import {ManageMeshComponentsComponent} from "./projectmesh/manage-components/manage-mesh-components.component";
import {MeshComponentActionsComponent} from "./projectmesh/mesh-component-actions/mesh-component-actions.component";
import {ProjectMeshActionsComponent} from "./projectmesh/project-mesh-actions/project-mesh-actions.component";
import {ProjectMeshService} from "./projectmesh/project-mesh.service";
import {RestService} from "./rest/rest.service";
import {AdminGuardService} from "./session/admin-guard.service";
import {AuthGuardService} from "./session/auth-guard.service";
import {AuthInterceptor} from "./session/auth.interceptor";
import {AuthService} from "./session/auth.service";
import {IsAdminOrDoerGuardService} from "./session/is-admin-or-doer-guard.service";
import {UserEditDialog} from "./user/edit-dialog/user-edit-dialog.component";
import {UserListComponent} from "./user/list/user-list.component";
import {MeComponent} from "./user/me/me.component";
import {UserService} from "./user/user.service";
import {ConfirmDialog} from "./util/confirm-dialog/confirm-dialog.component";
import {ConfirmWithTextDialog} from "./util/confirm-with-text-dialog/confirm-with-text-dialog.component";
import {TimeoutWithUiComponent} from "./util/timeout-with-ui/timeout-with-ui.component";
import {TimeoutSnackbarComponent} from "./util/timout-snackbar/timeout.snackbar.component";
import {EmailAvailableValidator} from "./util/validators/email-available.validator";
import {ForbiddenValuesValidator} from "./util/validators/forbidden-values-validator.directive";
import {MustMatchValidatorDirective} from "./util/validators/must-match.validator";
import {UsernameAvailableValidator} from "./util/validators/username-available.validator";
import {WebSocketServiceWrapper} from "./websocket/web-socket-service-wrapper.service";
import {WebSocketService} from "./websocket/web-socket.service";
import {configureSvgIcons} from "./configuration/configuration";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    TimeoutWithUiComponent,
    TimeoutSnackbarComponent,
    NavigationComponent,
    BreadcrumbComponent,
    UserListComponent,
    UserEditDialog,
    ConfirmDialog,
    MustMatchValidatorDirective,
    MeComponent,
    UsernameAvailableValidator,
    EmailAvailableValidator,
    ConfirmWithTextDialog,
    ShowDeployableConfigurationDialog,
    LoginComponent,
    DockerRegistryListComponent,
    DockerRegistryEditDialogComponent,
    ConfirmDeletionDialogComponent,
    KeyValueInputComponent,
    LifetimeBehaviourInputComponent,
    DeploymentBehaviourInputComponent,
    DeployableStatusComponent,
    ActivityLogComponent,
    FileUploadComponent,
    TemplateEditorComponent,
    ForbiddenValuesValidator,
    ProjectListComponent,
    EditProjectComponent,
    ProjectVersionListComponent,
    EditProjectVersionComponent,
    CreateProjectDialogComponent,
    LogsComponent,
    ProjectDashboardComponent,
    ProjectVersionActionsComponent,
    DefinedNamespacesListComponent,
    CreateNamespaceDialogComponent,
    DeleteNamespaceDialogComponent,
    TemplateVariablesComponent,
    TemplateVariableEditorComponent,
    ValueInputComponent,
    ProjectMeshListComponent,
    CreateProjectMeshDialogComponent,
    EditMeshComponent,
    ManageMeshComponentsComponent,
    CreateMeshComponentDialogComponent,
    ProjectMeshActionsComponent,
    DeploymentsDashboardComponent,
    ProjectmeshDashboardComponent,
    MeshComponentActionsComponent,
    DeployableActionsComponent,
    ProjectVersionVariableActionsComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FlexLayoutModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatExpansionModule,
    FormsModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatDialogModule,
    MatSelectModule,
    MatRippleModule,
    MatDividerModule,
    MatSortModule,
    MatInputModule,
    MatSlideToggleModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatMenuModule,
    FormsModule,
    HttpClientModule,
    AceEditorModule,
    MatToolbarModule,
    MatListModule,
    MatTabsModule,
    MatSidenavModule,
    MatChipsModule,
    MatStepperModule,
    InfiniteScrollModule,
    MatCheckboxModule,
    MatRadioModule,
    NgxMatSelectSearchModule
  ],
  providers: [
    RestService,
    AuthService,
    AuthGuardService,
    AdminGuardService,
    IsAdminOrDoerGuardService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: MAT_TOOLTIP_DEFAULT_OPTIONS,
      useValue: <MatTooltipDefaultOptions>{
        showDelay: 500
      }
    },
    UserService,
    ProjectService,
    WebSocketService,
    WebSocketServiceWrapper,
    ProjectMeshService,
  ],
  entryComponents: [
    TimeoutSnackbarComponent,
    UserEditDialog,
    ShowDeployableConfigurationDialog,
    ConfirmDialog,
    ConfirmWithTextDialog,
    DockerRegistryEditDialogComponent,
    ConfirmDeletionDialogComponent,
    LogsComponent,
    CreateProjectDialogComponent,
    CreateNamespaceDialogComponent,
    DeleteNamespaceDialogComponent,
    CreateProjectMeshDialogComponent,
    CreateMeshComponentDialogComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(matIconRegistry: MatIconRegistry, domSanitizer: DomSanitizer, rest: RestService, auth: AuthService, ws: WebSocketService) {
    configureSvgIcons(matIconRegistry, domSanitizer);

    auth.isAuthenticated().subscribe(authenticated => {
      if (authenticated) {
        ws.connect();
      } else {
        ws.close();
      }
    });

    rest.isLoggedIn().subscribe(user => {
      if (user) {
        auth.setAuthenticated(true);
      }
    }, () => {
      auth.setAuthenticated(false);
    });
  }
}
