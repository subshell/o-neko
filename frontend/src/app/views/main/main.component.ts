import {Component, ViewChild} from "@angular/core";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";
import {NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";
import {MatSidenav} from "@angular/material/sidenav";
import {ExpandableMenuEntry} from "../../components/expandable-menu/expandable-menu";

@Component({
  selector: 'on-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
export class MainComponent {
  private readonly mobileBreakpoints = [Breakpoints.Small, Breakpoints.XSmall];

  @ViewChild(MatSidenav, {static: true}) drawer: MatSidenav;
  public isMobile = this.breakpointObserver.isMatched(this.mobileBreakpoints);

  public menuStructure: Array<ExpandableMenuEntry> = [
    {
      title: 'Project management',
      icon: 'dashboard',
      children: [{
        title: 'Active Deployments',
        href: ''
      }, {
        title: 'Projects',
        href: '/projects'
      }]
    },
    {
      title: 'Administration',
      icon: 'supervised-user-circle',
      children: [{
        title: 'Users',
        href: '/users'
      }]
    }
  ];

  constructor(private breakpointObserver: BreakpointObserver,
              router: Router) {
    breakpointObserver.observe(this.mobileBreakpoints).subscribe(result => {
      this.isMobile = result.matches;
    });
    router.events.pipe(
      filter(event => this.isMobile && event instanceof NavigationEnd)
    ).subscribe(() => this.drawer.close());
  }
}
