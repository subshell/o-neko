import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {NgModule} from '@angular/core';
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatLegacyCheckboxModule as MatCheckboxModule} from "@angular/material/legacy-checkbox";
import {MatLegacyRadioModule as MatRadioModule} from "@angular/material/legacy-radio";
import {MatLegacyButtonModule as MatButtonModule} from "@angular/material/legacy-button";
import {MatLegacyCardModule as MatCardModule} from "@angular/material/legacy-card";
import {MatLegacyChipsModule as MatChipsModule} from "@angular/material/legacy-chips";
import {MatRippleModule} from "@angular/material/core";
import {MatLegacyDialogModule as MatDialogModule} from "@angular/material/legacy-dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatLegacyFormFieldModule as MatFormFieldModule} from "@angular/material/legacy-form-field";
import {MatIconModule, MatIconRegistry} from "@angular/material/icon";
import {MatLegacyInputModule as MatInputModule} from "@angular/material/legacy-input";
import {MatLegacyListModule as MatListModule} from "@angular/material/legacy-list";
import {MatLegacyMenuModule as MatMenuModule} from "@angular/material/legacy-menu";
import {MatLegacyPaginatorIntl as MatPaginatorIntl, MatLegacyPaginatorModule as MatPaginatorModule} from "@angular/material/legacy-paginator";
import {MatLegacyProgressSpinnerModule as MatProgressSpinnerModule} from "@angular/material/legacy-progress-spinner";
import {MatLegacySelectModule as MatSelectModule} from "@angular/material/legacy-select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatLegacySlideToggleModule as MatSlideToggleModule} from "@angular/material/legacy-slide-toggle";
import {MatLegacySnackBarModule as MatSnackBarModule} from "@angular/material/legacy-snack-bar";
import {MatSortModule} from "@angular/material/sort";
import {MatStepperModule} from "@angular/material/stepper";
import {MatLegacyTabsModule as MatTabsModule} from "@angular/material/legacy-tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {
  MAT_LEGACY_TOOLTIP_DEFAULT_OPTIONS as MAT_TOOLTIP_DEFAULT_OPTIONS,
  MatLegacyTooltipDefaultOptions as MatTooltipDefaultOptions,
  MatLegacyTooltipModule as MatTooltipModule
} from "@angular/material/legacy-tooltip";
import {BrowserModule, DomSanitizer} from '@angular/platform-browser';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {ActivityLogComponent} from "./activity/activity-log/activity-log.component";
import {AppRoutingModule} from "./app-routes.module";

import {AppComponent} from './app.component';
import {BreadcrumbComponent} from "./components/breadcrumbs/breadcrumb.component";
import {DeployableActionsComponent} from "./deployable/deployable-actions/deployable-actions.component";
import {DeployableStatusComponent} from "./deployable/deployable-status/deployable-status.component";
import {DeploymentsDashboardComponent} from "./deployable/deployments-dashboard/deployments-dashboard.component";
import {ShowDeployableConfigurationDialog} from "./deployable/show-deployable-configuration-dialog/show-deployable-configuration-dialog.component";
import {TemplateEditorComponent} from "./deployable/template-editor/template-editor.component";
import {ConfirmDeletionDialogComponent} from "./registries/confirm-deletion.dialog/confirm-deletion-dialog.component";
import {DockerRegistryEditDialogComponent} from "./registries/docker/edit-dialog/docker-registry-edit-dialog.component";
import {DockerRegistryListComponent} from "./registries/docker/list/docker-registry-list.component";
import {DeploymentBehaviourInputComponent} from "./form/deployment-behaviour/deployment-behaviour-input.component";
import {KeyValueInputComponent} from "./form/key-value-input/key-value-input.component";
import {LifetimeBehaviourInputComponent} from "./form/lifetime-behaviour/lifetime-behaviour-input.component";
import {FileUploadComponent} from "./form/upload/file-upload.component";
import {ValueInputComponent} from './form/value-input/value-input.component';
import {HomeComponent} from "./home/home.component";
import {LoginComponent} from "./views/login/login.component";
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
import {MonacoEditorModule} from "ngx-monaco-editor";
import {configureMatPaginatorI18n, configureSvgIcons, configureTranslations, provideAnimationDriverBasedOnUserPreferences} from "./configuration/configuration";
import {MainComponent} from "./views/main/main.component";
import {ExpandableMenuComponent} from "./components/expandable-menu/expandable-menu.component";
import {NgxsModule, Store} from "@ngxs/store";
import {environment} from "../environments/environment.prod";
import {NgxsStoragePluginModule} from "@ngxs/storage-plugin";
import {NgxsReduxDevtoolsPluginModule} from "@ngxs/devtools-plugin";
import {NgxsLoggerPluginModule} from "@ngxs/logger-plugin";
import {AnimationDriver} from "@angular/animations/browser";
import {appStates} from "./store";
import {ThemingState} from "./store/theming/theming.state";
import {ThemeSwitcherComponent} from "./components/theme-switcher/theme-switcher.component";
import {DndDirective} from './form/upload/dnd.directive';
import {
  EditConfigurationTemplateDialogComponent
} from './deployable/template-editor/edit-configuration-template-dialog/edit-configuration-template-dialog.component';
import {FooterComponent} from "./components/footer/footer.component";
import {TranslateCompiler, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {AliasingTranslateCompiler} from "./util/aliasing-translate-compiler";
import {HelmRegistryListComponent} from "./registries/helm/list/helm-registry-list.component";
import {HelmRegistryEditDialogComponent} from "./registries/helm/edit-dialog/helm-registry-edit-dialog.component";
import {DistinctObjectArrayPipe} from "./util/distinct-object-array.pipe";
import {FilterDeepPipe} from "./util/filter-deep.pipe";
import {I18nSwitcherComponent} from "./components/i18n-switcher/i18n-switcher.component";
import {I18nState} from "./store/i18n/i18n.state";
import {UrlTemplatesComponent} from "./form/url-templates-input/url-templates.component";
import {GlobalSearchComponent} from "./navigation/global-search/global-search.component";
import {CachingProjectRestClient} from "./rest/caching-project-rest-client";
import {MultiDeployActionsComponent} from "./deployable/multi-deploy-actions/multi-deploy-actions.component";

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, '/assets/i18n/', '.json');
}

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
    DeploymentsDashboardComponent,
    DeployableActionsComponent,
    ProjectVersionVariableActionsComponent,
    MainComponent,
    ExpandableMenuComponent,
    ThemeSwitcherComponent,
    DndDirective,
    EditConfigurationTemplateDialogComponent,
    FooterComponent,
    HelmRegistryListComponent,
    HelmRegistryEditDialogComponent,
    DistinctObjectArrayPipe,
    FilterDeepPipe,
    I18nSwitcherComponent,
    UrlTemplatesComponent,
    GlobalSearchComponent,
    MultiDeployActionsComponent
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
    MonacoEditorModule.forRoot(),
    MatToolbarModule,
    MatListModule,
    MatTabsModule,
    MatSidenavModule,
    MatChipsModule,
    MatStepperModule,
    MatCheckboxModule,
    MatRadioModule,
    NgxMatSelectSearchModule,
    NgxsModule.forRoot(appStates, {developmentMode: !environment.production}),
    NgxsStoragePluginModule.forRoot({key: [ThemingState, I18nState]}),
    NgxsReduxDevtoolsPluginModule.forRoot(),
    NgxsLoggerPluginModule.forRoot(),
    TranslateModule.forRoot({
      compiler: {
        provide: TranslateCompiler,
        useValue: new AliasingTranslateCompiler()
      },
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
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
    {
      provide: MatPaginatorIntl,
      deps: [TranslateService],
      useFactory: (translate) => configureMatPaginatorI18n(translate),
    },
    UserService,
    ProjectService,
    WebSocketService,
    WebSocketServiceWrapper,
    {
      provide: AnimationDriver,
      useFactory: () => provideAnimationDriverBasedOnUserPreferences()
    },
    CachingProjectRestClient
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(translateService: TranslateService,
              matIconRegistry: MatIconRegistry,
              domSanitizer: DomSanitizer,
              rest: RestService,
              auth: AuthService,
              ws: WebSocketService,
              store: Store) {
    configureSvgIcons(matIconRegistry, domSanitizer);
    configureTranslations(translateService, store);

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
