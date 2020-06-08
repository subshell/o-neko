import {Component, ViewChild} from "@angular/core";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";
import {NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";
import {MatSidenav} from "@angular/material/sidenav";
import {ExpandableMenuEntry, SingleMenuEntry} from "../../components/expandable-menu/expandable-menu";
import {RestService} from "../../rest/rest.service";
import {UserRole} from "../../user/user-role";

@Component({
  selector: 'on-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
export class MainComponent {
  private readonly mobileBreakpoints = [Breakpoints.Small, Breakpoints.XSmall];

  @ViewChild(MatSidenav, {static: true}) drawer: MatSidenav;
  public isMobile = this.breakpointObserver.isMatched(this.mobileBreakpoints);

  public menuStructure: Array<ExpandableMenuEntry | SingleMenuEntry>;

  constructor(private breakpointObserver: BreakpointObserver,
              router: Router,
              private rest: RestService) {

    breakpointObserver.observe(this.mobileBreakpoints).subscribe(result => {
      this.isMobile = result.matches;
    });
    router.events.pipe(
      filter(event => this.isMobile && event instanceof NavigationEnd)
    ).subscribe(() => this.drawer.close());
    this.initMenu();
  }

  private initMenu() {
    this.rest.currentUser().subscribe(user => {
      this.menuStructure = [
        {
          title: 'Home',
          icon: 'home',
          href: '',
          isSingleEntry: true
        },
        {
          title: 'Project Management',
          icon: 'folder',
          children: [{
            title: 'Projects',
            href: '/projects'
          },{
            title: 'Project Meshes',
            href: '/project-meshes'
          },{
            title: 'Namespaces',
            href: '/namespaces',
            hidden: !user.hasAnyPermission(UserRole.ADMIN, UserRole.DOER)
          }]
        },
        {
          title: 'Administration',
          icon: 'settings',
          hidden: !user.hasAnyPermission(UserRole.ADMIN, UserRole.DOER),
          children: [{
            title: 'Docker Registries',
            href: '/docker-registries',
            hidden: !user.hasAnyPermission(UserRole.ADMIN, UserRole.DOER)
          },{
            title: 'Users',
            href: '/users',
            hidden: !user.hasAnyPermission(UserRole.ADMIN)
          },{
            title: 'Activity Log',
            href: '/logs',
            hidden: !user.hasAnyPermission(UserRole.ADMIN, UserRole.DOER)
          }]
        }
      ];
    });
  }
}
