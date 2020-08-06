import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {CdkAccordionItem} from '@angular/cdk/accordion';
import {matExpansionAnimations, MatExpansionPanelState} from '@angular/material/expansion';
import {ExpandableMenuEntry} from './expandable-menu';
import {NavigationEnd, Router, RouterEvent} from '@angular/router';
import {UniqueSelectionDispatcher} from '@angular/cdk/collections';
import {map, startWith, tap} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Component({
  selector: 'on-expandable-menu',
  templateUrl: './expandable-menu.component.html',
  styleUrls: ['./expandable-menu.component.scss'],
  animations: [matExpansionAnimations.bodyExpansion, matExpansionAnimations.indicatorRotate]
})
export class ExpandableMenuComponent extends CdkAccordionItem implements OnInit {

  @Input() entry: ExpandableMenuEntry;
  @Input() expanded: boolean;
  subRouteIsActive: Observable<boolean>;

  constructor(changeDetectorRef: ChangeDetectorRef,
              expansionDispatcher: UniqueSelectionDispatcher,
              private router: Router) {
    super(null, changeDetectorRef, expansionDispatcher);
  }

  getExpandedState(): MatExpansionPanelState {
    return this.expanded ? 'expanded' : 'collapsed';
  }

  ngOnInit(): void {
    this.subRouteIsActive = this.router.events.pipe(
      map((event: RouterEvent) => {
        if (event instanceof NavigationEnd) {
          return this.isSubRouteActive(event.url);
        }
        return false;
      }),
      startWith(this.isSubRouteActive(this.router.url)),
      tap(isActive => {
        this.expanded = isActive;
      })
    );
  }

  private isSubRouteActive(url: string): boolean {
    return this.entry.children.map(child => child.href).some(href => this.shouldPathBeMatched(href, url));
  }

  private shouldPathBeMatched(path: string, url: string): boolean {
    if (path === '' || path === '/') {
      return url === '' || url === '/';
    }

    path = path.endsWith('/') ? path.substring(0, path.length - 1) : path;
    url = url.endsWith('/') ? url.substring(0, url.length - 1) : url;
    return url.startsWith(path);
  }
}
